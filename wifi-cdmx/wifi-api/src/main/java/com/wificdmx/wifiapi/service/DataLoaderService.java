package com.wificdmx.wifiapi.service;

import com.wificdmx.wifiapi.model.WifiPoint;
import com.wificdmx.wifiapi.repository.WifiPointRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for loading WiFi points data from Excel file into the database.
 * This service automatically executes on application startup using @PostConstruct.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoaderService {

    private final WifiPointRepository wifiPointRepository;
    private static final String EXCEL_FILE_PATH = "data/00-2025-wifi_gratuito_en_cdmx.xlsx";

    /**
     * Loads data from Excel file into database if the database is empty.
     * Executes automatically after bean construction.
     */
    @PostConstruct
    public void loadData() {
        // Only load data if the database is empty
        if (wifiPointRepository.count() > 0) {
            log.info("Database already contains {} WiFi points. Skipping data load.",
                    wifiPointRepository.count());
            return;
        }

        log.info("Starting data load from Excel file: {}", EXCEL_FILE_PATH);

        try {
            List<WifiPoint> wifiPoints = readExcelFile();

            if (wifiPoints.isEmpty()) {
                log.warn("No data found in Excel file");
                return;
            }

            log.info("Saving {} WiFi points to database...", wifiPoints.size());
            wifiPointRepository.saveAll(wifiPoints);
            log.info("Successfully loaded {} WiFi points into database", wifiPoints.size());

        } catch (IOException e) {
            log.error("Error loading data from Excel file: {}", e.getMessage(), e);
        }
    }

    /**
     * Reads WiFi points data from Excel file.
     * Expected columns: id, programa, latitud, longitud, alcaldia
     *
     * @return List of WifiPoint entities
     * @throws IOException if file cannot be read
     */
    private List<WifiPoint> readExcelFile() throws IOException {
        List<WifiPoint> wifiPoints = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource(EXCEL_FILE_PATH);

        try (InputStream inputStream = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();

            log.info("Reading Excel file with {} rows", totalRows);

            // Skip header row (index 0) and start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                try {
                    WifiPoint wifiPoint = createWifiPointFromRow(row);
                    if (wifiPoint != null) {
                        wifiPoints.add(wifiPoint);
                    }

                    // Log progress every 5000 records
                    if (i % 5000 == 0) {
                        log.info("Processed {} / {} rows", i, totalRows - 1);
                    }

                } catch (Exception e) {
                    log.warn("Error processing row {}: {}", i + 1, e.getMessage());
                }
            }
        }

        return wifiPoints;
    }

    /**
     * Creates a WifiPoint entity from an Excel row.
     * Handles data type conversions and normalization.
     *
     * @param row Excel row containing WiFi point data
     * @return WifiPoint entity or null if data is invalid
     */
    private WifiPoint createWifiPointFromRow(Row row) {
        try {
            // Column 0: id (String)
            String puntoId = getCellValueAsString(row.getCell(0));
            if (puntoId == null || puntoId.trim().isEmpty()) {
                return null;
            }

            // Column 1: programa (String)
            String programa = getCellValueAsString(row.getCell(1));
            if (programa == null || programa.trim().isEmpty()) {
                return null;
            }

            // Column 2: latitud (Double - may be stored as String in Excel)
            Double latitud = getCellValueAsDouble(row.getCell(2));
            if (latitud == null) {
                return null;
            }

            // Column 3: longitud (Double)
            Double longitud = getCellValueAsDouble(row.getCell(3));
            if (longitud == null) {
                return null;
            }

            // Column 4: alcaldia (String) - normalize to title case
            String alcaldia = getCellValueAsString(row.getCell(4));
            if (alcaldia == null || alcaldia.trim().isEmpty()) {
                return null;
            }
            alcaldia = normalizeAlcaldia(alcaldia);

            WifiPoint wifiPoint = new WifiPoint();
            wifiPoint.setPuntoId(puntoId.trim());
            wifiPoint.setPrograma(programa.trim());
            wifiPoint.setLatitud(latitud);
            wifiPoint.setLongitud(longitud);
            wifiPoint.setAlcaldia(alcaldia);

            return wifiPoint;

        } catch (Exception e) {
            log.warn("Error creating WifiPoint from row: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts cell value as String, handling different cell types.
     *
     * @param cell Excel cell
     * @return String value or null
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle numeric values that should be strings (like IDs)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Extracts cell value as Double, handling String to Double conversion.
     *
     * @param cell Excel cell
     * @return Double value or null
     */
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    // Try to parse string as double
                    String value = cell.getStringCellValue().trim();
                    if (!value.isEmpty()) {
                        return Double.parseDouble(value);
                    }
                    return null;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Cannot convert cell value to Double: {}", cell.toString());
            return null;
        }
    }

    /**
     * Normalizes alcaldia names to title case.
     * Converts "IZTAPALAPA" to "Iztapalapa", "MIGUEL HIDALGO" to "Miguel Hidalgo", etc.
     *
     * @param alcaldia Raw alcaldia name
     * @return Normalized alcaldia name
     */
    private String normalizeAlcaldia(String alcaldia) {
        if (alcaldia == null || alcaldia.trim().isEmpty()) {
            return alcaldia;
        }

        // Split by spaces and capitalize first letter of each word
        String[] words = alcaldia.trim().toLowerCase().split("\\s+");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                normalized.append(" ");
            }

            String word = words[i];
            if (!word.isEmpty()) {
                // Capitalize first letter
                normalized.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    normalized.append(word.substring(1));
                }
            }
        }

        return normalized.toString();
    }
}

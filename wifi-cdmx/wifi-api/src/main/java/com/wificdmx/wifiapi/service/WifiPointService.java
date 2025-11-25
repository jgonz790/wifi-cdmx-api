package com.wificdmx.wifiapi.service;

import com.wificdmx.wifiapi.dto.WifiPointDTO;
import com.wificdmx.wifiapi.dto.WifiPointResponseDTO;
import com.wificdmx.wifiapi.exception.ResourceNotFoundException;
import com.wificdmx.wifiapi.model.WifiPoint;
import com.wificdmx.wifiapi.repository.WifiPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for WiFi Point operations.
 * Handles business logic and coordinates between controllers and repositories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WifiPointService {

    private final WifiPointRepository wifiPointRepository;

    /**
     * Retrieves all WiFi points with pagination.
     *
     * @param pageable Pagination parameters
     * @return Paginated response with WiFi points
     */
    public WifiPointResponseDTO findAll(Pageable pageable) {
        log.debug("Finding all WiFi points - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<WifiPoint> page = wifiPointRepository.findAll(pageable);
        List<WifiPointDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return buildResponse(content, page);
    }

    /**
     * Finds a WiFi point by its ID.
     *
     * @param id WiFi point ID
     * @return WiFi point DTO
     * @throws ResourceNotFoundException if WiFi point not found
     */
    public WifiPointDTO findById(String id) {
        log.debug("Finding WiFi point by ID: {}", id);

        WifiPoint wifiPoint = wifiPointRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("WiFi point not found with ID: {}", id);
                    return new ResourceNotFoundException("WiFi point not found with ID: " + id);
                });

        return convertToDTO(wifiPoint);
    }

    /**
     * Finds WiFi points by alcaldia with pagination.
     *
     * @param alcaldia Alcaldia name
     * @param pageable Pagination parameters
     * @return Paginated response with WiFi points
     */
    public WifiPointResponseDTO findByAlcaldia(String alcaldia, Pageable pageable) {
        log.debug("Finding WiFi points by alcaldia: {} - Page: {}, Size: {}",
                alcaldia, pageable.getPageNumber(), pageable.getPageSize());

        Page<WifiPoint> page = wifiPointRepository.findByAlcaldiaIgnoreCase(alcaldia, pageable);
        List<WifiPointDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return buildResponse(content, page);
    }

    /**
     * Finds nearby WiFi points using Haversine formula.
     * Returns WiFi points ordered by distance from the given coordinates.
     *
     * @param lat Latitude of reference point
     * @param lon Longitude of reference point
     * @param pageable Pagination parameters
     * @return Paginated response with WiFi points including distance
     */
    @Transactional(readOnly = true)
    public Page<WifiPointDTO> findNearby(Double lat, Double lon, Pageable pageable) {
        log.debug("Finding nearby WiFi points - Lat: {}, Lon: {}, Page: {}, Size: {}",
                lat, lon, pageable.getPageNumber(), pageable.getPageSize());

        Page<Object[]> results = wifiPointRepository.findNearbyPoints(lat, lon, pageable);

        List<WifiPointDTO> dtos = results.getContent().stream()
                .map(this::convertNearbyResultToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    /**
     * Converts WifiPoint entity to DTO.
     *
     * @param wifiPoint Entity to convert
     * @return WifiPointDTO
     */
    private WifiPointDTO convertToDTO(WifiPoint wifiPoint) {
        return WifiPointDTO.builder()
                .puntoId(wifiPoint.getPuntoId())
                .programa(wifiPoint.getPrograma())
                .latitud(wifiPoint.getLatitud())
                .longitud(wifiPoint.getLongitud())
                .alcaldia(wifiPoint.getAlcaldia())
                .build();
    }

    /**
     * Converts nearby query result (Object[]) to DTO with distance.
     * The Object[] contains: [punto_id, programa, latitud, longitud, alcaldia, distancia]
     *
     * @param result Query result array
     * @return WifiPointDTO with distance
     */
    private WifiPointDTO convertNearbyResultToDTO(Object[] result) {
        return WifiPointDTO.builder()
                .puntoId((String) result[0])
                .programa((String) result[1])
                .latitud((Double) result[2])
                .longitud((Double) result[3])
                .alcaldia((String) result[4])
                .distancia(result[5] != null ? ((Number) result[5]).doubleValue() : null)
                .build();
    }

    /**
     * Builds a standardized response DTO from page data.
     *
     * @param content List of WiFi point DTOs
     * @param page Page object with metadata
     * @return WifiPointResponseDTO
     */
    private WifiPointResponseDTO buildResponse(List<WifiPointDTO> content, Page<?> page) {
        return WifiPointResponseDTO.builder()
                .content(content)
                .totalElements((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public long count() {
        return wifiPointRepository.count();
    }
}
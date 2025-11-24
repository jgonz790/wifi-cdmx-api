package com.wificdmx.wifiapi.controller;

import com.wificdmx.wifiapi.dto.WifiPointDTO;
import com.wificdmx.wifiapi.dto.WifiPointResponseDTO;
import com.wificdmx.wifiapi.service.WifiPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for WiFi Point operations.
 * Provides endpoints for querying WiFi access points in CDMX.
 */
@RestController
@RequestMapping("/api/v1/wifi-points")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WiFi Points", description = "API endpoints for managing WiFi access points in CDMX")
public class WifiPointController {

    private final WifiPointService wifiPointService;

    /**
     * Get all WiFi points with pagination.
     *
     * @param pageable Pagination parameters (page, size, sort)
     * @return Paginated list of WiFi points
     */
    @GetMapping
    @Operation(
            summary = "Get all WiFi points",
            description = "Retrieves a paginated list of all WiFi access points in CDMX"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved WiFi points",
                    content = @Content(schema = @Schema(implementation = WifiPointResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<WifiPointResponseDTO> getAllWifiPoints(
            @PageableDefault(size = 20, sort = "puntoId")
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        log.info("GET /api/v1/wifi-points - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        WifiPointResponseDTO response = wifiPointService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get WiFi point by ID.
     *
     * @param id WiFi point unique identifier
     * @return WiFi point details
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get WiFi point by ID",
            description = "Retrieves detailed information about a specific WiFi point"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "WiFi point found",
                    content = @Content(schema = @Schema(implementation = WifiPointDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "WiFi point not found")
    })
    public ResponseEntity<WifiPointDTO> getWifiPointById(
            @PathVariable
            @Parameter(description = "WiFi point unique identifier", example = "PILARES-001")
            String id
    ) {
        log.info("GET /api/v1/wifi-points/{}", id);
        WifiPointDTO wifiPoint = wifiPointService.findById(id);
        return ResponseEntity.ok(wifiPoint);
    }

    /**
     * Get WiFi points by alcaldia (borough).
     *
     * @param alcaldia Alcaldia name
     * @param pageable Pagination parameters
     * @return Paginated list of WiFi points in the specified alcaldia
     */
    @GetMapping("/alcaldia/{alcaldia}")
    @Operation(
            summary = "Get WiFi points by alcaldia",
            description = "Retrieves WiFi points filtered by alcaldia (borough) with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved WiFi points",
                    content = @Content(schema = @Schema(implementation = WifiPointResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public ResponseEntity<WifiPointResponseDTO> getWifiPointsByAlcaldia(
            @PathVariable
            @Parameter(description = "Alcaldia name", example = "Iztapalapa")
            String alcaldia,
            @PageableDefault(size = 20, sort = "puntoId")
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        log.info("GET /api/v1/wifi-points/alcaldia/{} - Page: {}, Size: {}",
                alcaldia, pageable.getPageNumber(), pageable.getPageSize());
        WifiPointResponseDTO response = wifiPointService.findByAlcaldia(alcaldia, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get nearby WiFi points based on coordinates.
     * Returns WiFi points ordered by distance from the specified location.
     *
     * @param lat Latitude of reference point
     * @param lon Longitude of reference point
     * @param pageable Pagination parameters
     * @return Paginated list of WiFi points with distance information
     */
    @GetMapping("/nearby")
    @Operation(
            summary = "Find nearby WiFi points",
            description = "Finds WiFi points near the specified coordinates using Haversine formula. Returns results ordered by distance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved nearby WiFi points",
                    content = @Content(schema = @Schema(implementation = WifiPointResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates or parameters")
    })
    public ResponseEntity<WifiPointResponseDTO> getNearbyWifiPoints(
            @RequestParam
            @Parameter(description = "Latitude (-90 to 90)", example = "19.4326", required = true)
            Double lat,
            @RequestParam
            @Parameter(description = "Longitude (-180 to 180)", example = "-99.1332", required = true)
            Double lon,
            @PageableDefault(size = 20)
            @Parameter(description = "Pagination parameters (page, size)")
            Pageable pageable
    ) {
        log.info("GET /api/v1/wifi-points/nearby?lat={}&lon={} - Page: {}, Size: {}",
                lat, lon, pageable.getPageNumber(), pageable.getPageSize());
        WifiPointResponseDTO response = wifiPointService.findNearby(lat, lon, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint to verify API is running.
     *
     * @return Simple status message
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verifies that the API is running properly"
    )
    @ApiResponse(responseCode = "200", description = "API is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "WiFi CDMX API is running");
        response.put("totalPoints", wifiPointService.count());
        response.put("developer", "Osvaldo González");

        return ResponseEntity.ok(response);
    }
}

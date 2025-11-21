package com.wificdmx.wifiapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for WiFi Point information.
 * Used to transfer WiFi point data between layers and to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WifiPointDTO {

    /**
     * Unique identifier for the WiFi point
     */
    private String puntoId;

    /**
     * Program name that manages this WiFi point
     */
    private String programa;

    /**
     * Latitude coordinate
     */
    private Double latitud;

    /**
     * Longitude coordinate
     */
    private Double longitud;

    /**
     * Alcaldia (borough) where the WiFi point is located
     */
    private String alcaldia;

    /**
     * Distance in kilometers from a reference point.
     * Only populated when querying nearby WiFi points.
     */
    private Double distancia;
}

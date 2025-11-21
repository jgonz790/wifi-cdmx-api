package com.wificdmx.wifiapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for paginated WiFi Point queries.
 * Contains the list of WiFi points and pagination metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WifiPointResponseDTO {

    /**
     * List of WiFi points in the current page
     */
    private List<WifiPointDTO> content;

    /**
     * Total number of elements across all pages
     */
    private int totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Current page number (zero-based)
     */
    private int currentPage;

    /**
     * Number of elements in the current page
     */
    private int pageSize;

    /**
     * Whether this is the first page
     */
    private boolean first;

    /**
     * Whether this is the last page
     */
    private boolean last;
}

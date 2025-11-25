package com.wificdmx.wifiapi.repository;

import com.wificdmx.wifiapi.model.WifiPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WifiPointRepository extends JpaRepository<WifiPoint, String> {

    /**
     * Finds WiFi points by alcaldia with pagination
     *
     * @param alcaldia Alcaldia name
     * @param pageable Pagination parameters
     * @return Page of WiFi points
     */
    Page<WifiPoint> findByAlcaldiaIgnoreCase(String alcaldia, Pageable pageable);

    /**
     * Finds nearby WiFi points using Haversine formula
     * Returns WiFi points ordered by distance from the given coordinates
     *
     * @param lat Latitude of reference point
     * @param lon Longitude of reference point
     * @param pageable Pagination parameters
     * @return List of Object arrays containing [WifiPoint, distance]
     */

    /**
     * Finds nearby WiFi points using Haversine formula
     * Returns WiFi points ordered by distance from the given coordinates
     *
     * @param lat Latitude of reference point
     * @param lon Longitude of reference point
     * @param pageable Pagination parameters
     * @return Page of WifiPoint objects
     */
    @Query(value = """
        SELECT w.punto_id, w.programa, w.latitud, w.longitud, w.alcaldia,
               (6371 * acos(
                   cos(radians(:lat)) * cos(radians(w.latitud)) *
                   cos(radians(w.longitud) - radians(:lon)) +
                   sin(radians(:lat)) * sin(radians(w.latitud))
               )) AS distancia
        FROM wifi_points w
        ORDER BY distancia
        """,
            countQuery = "SELECT count(*) FROM wifi_points",
            nativeQuery = true)
    Page<Object[]> findNearbyPoints(
            @Param("lat") Double lat,
            @Param("lon") Double lon,
            Pageable pageable
    );

    /**
     * Counts total WiFi points (for pagination of nearby query)
     */
    @Query("SELECT COUNT(w) FROM WifiPoint w")
    long countAll();
}
package com.wificdmx.wifiapi.service;

import com.wificdmx.wifiapi.dto.WifiPointDTO;
import com.wificdmx.wifiapi.dto.WifiPointResponseDTO;
import com.wificdmx.wifiapi.exception.ResourceNotFoundException;
import com.wificdmx.wifiapi.model.WifiPoint;
import com.wificdmx.wifiapi.repository.WifiPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WifiPointService.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WifiPointService Tests")
class WifiPointServiceTest {

    @Mock
    private WifiPointRepository wifiPointRepository;

    @InjectMocks
    private WifiPointService wifiPointService;

    private WifiPoint wifiPoint1;
    private WifiPoint wifiPoint2;
    private WifiPoint wifiPoint3;

    @BeforeEach
    void setUp() {
        wifiPoint1 = new WifiPoint();
        wifiPoint1.setPuntoId("PILARES-001");
        wifiPoint1.setPrograma("Pilares");
        wifiPoint1.setLatitud(19.4326);
        wifiPoint1.setLongitud(-99.1332);
        wifiPoint1.setAlcaldia("Iztapalapa");

        wifiPoint2 = new WifiPoint();
        wifiPoint2.setPuntoId("PILARES-002");
        wifiPoint2.setPrograma("Pilares");
        wifiPoint2.setLatitud(19.4350);
        wifiPoint2.setLongitud(-99.1400);
        wifiPoint2.setAlcaldia("Iztapalapa");

        wifiPoint3 = new WifiPoint();
        wifiPoint3.setPuntoId("FARO-001");
        wifiPoint3.setPrograma("Faros");
        wifiPoint3.setLatitud(19.4200);
        wifiPoint3.setLongitud(-99.1500);
        wifiPoint3.setAlcaldia("Benito Juarez");
    }

    @Test
    @DisplayName("Should find all WiFi points with pagination")
    void testFindAll() {
        // Arrange
        List<WifiPoint> wifiPoints = Arrays.asList(wifiPoint1, wifiPoint2, wifiPoint3);
        Page<WifiPoint> page = new PageImpl<>(wifiPoints, PageRequest.of(0, 20), wifiPoints.size());

        when(wifiPointRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        WifiPointResponseDTO response = wifiPointService.findAll(PageRequest.of(0, 20));

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(3, response.getContent().size());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        verify(wifiPointRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should find WiFi point by ID successfully")
    void testFindByIdSuccess() {
        // Arrange
        when(wifiPointRepository.findById("PILARES-001")).thenReturn(Optional.of(wifiPoint1));

        // Act
        WifiPointDTO result = wifiPointService.findById("PILARES-001");

        // Assert
        assertNotNull(result);
        assertEquals("PILARES-001", result.getPuntoId());
        assertEquals("Pilares", result.getPrograma());
        assertEquals(19.4326, result.getLatitud());
        assertEquals(-99.1332, result.getLongitud());
        assertEquals("Iztapalapa", result.getAlcaldia());

        verify(wifiPointRepository, times(1)).findById("PILARES-001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when WiFi point not found")
    void testFindByIdNotFound() {
        // Arrange
        when(wifiPointRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> wifiPointService.findById("NONEXISTENT-ID")
        );

        assertTrue(exception.getMessage().contains("WiFi point not found"));
        verify(wifiPointRepository, times(1)).findById("NONEXISTENT-ID");
    }

    @Test
    @DisplayName("Should find WiFi points by alcaldia with pagination")
    void testFindByAlcaldia() {
        // Arrange
        List<WifiPoint> iztapalapaPoints = Arrays.asList(wifiPoint1, wifiPoint2);
        Page<WifiPoint> page = new PageImpl<>(iztapalapaPoints, PageRequest.of(0, 20), iztapalapaPoints.size());

        when(wifiPointRepository.findByAlcaldiaIgnoreCase(anyString(), any(Pageable.class))).thenReturn(page);

        // Act
        WifiPointResponseDTO response = wifiPointService.findByAlcaldia("Iztapalapa", PageRequest.of(0, 20));

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getContent().size());

        response.getContent().forEach(dto -> assertEquals("Iztapalapa", dto.getAlcaldia()));

        verify(wifiPointRepository, times(1)).findByAlcaldiaIgnoreCase("Iztapalapa", PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("Should validate latitude range in findNearby")
    void testFindNearbyInvalidLatitude() {
        // Act & Assert - Latitude too high
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(91.0, -99.1332, PageRequest.of(0, 20))
        );
        assertTrue(exception1.getMessage().contains("Latitude must be between -90 and 90"));

        // Act & Assert - Latitude too low
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(-91.0, -99.1332, PageRequest.of(0, 20))
        );
        assertTrue(exception2.getMessage().contains("Latitude must be between -90 and 90"));

        verify(wifiPointRepository, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should validate longitude range in findNearby")
    void testFindNearbyInvalidLongitude() {
        // Act & Assert - Longitude too high
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(19.4326, 181.0, PageRequest.of(0, 20))
        );
        assertTrue(exception1.getMessage().contains("Longitude must be between -180 and 180"));

        // Act & Assert - Longitude too low
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(19.4326, -181.0, PageRequest.of(0, 20))
        );
        assertTrue(exception2.getMessage().contains("Longitude must be between -180 and 180"));

        verify(wifiPointRepository, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should require both coordinates in findNearby")
    void testFindNearbyNullCoordinates() {
        // Act & Assert - Null latitude
        IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(null, -99.1332, PageRequest.of(0, 20))
        );
        assertTrue(exception1.getMessage().contains("Latitude and longitude are required"));

        // Act & Assert - Null longitude
        IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> wifiPointService.findNearby(19.4326, null, PageRequest.of(0, 20))
        );
        assertTrue(exception2.getMessage().contains("Latitude and longitude are required"));

        verify(wifiPointRepository, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testFindAllEmptyResults() {
        // Arrange
        Page<WifiPoint> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 20), 0);
        when(wifiPointRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        WifiPointResponseDTO response = wifiPointService.findAll(PageRequest.of(0, 20));

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getContent().size());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    @DisplayName("Should handle pagination correctly for multiple pages")
    void testFindAllWithMultiplePages() {
        // Arrange - Page 1 of 2
        List<WifiPoint> firstPagePoints = Arrays.asList(wifiPoint1, wifiPoint2);
        Page<WifiPoint> firstPage = new PageImpl<>(firstPagePoints, PageRequest.of(0, 2), 3);

        when(wifiPointRepository.findAll(any(Pageable.class))).thenReturn(firstPage);

        // Act
        WifiPointResponseDTO response = wifiPointService.findAll(PageRequest.of(0, 2));

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(2, response.getContent().size());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());
    }
}

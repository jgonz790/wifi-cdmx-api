package com.wificdmx.wifiapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wifi_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WifiPoint {

    @Id
    @Column(name = "punto_id", length = 100)
    private String puntoId;

    @Column(name = "programa", length = 100, nullable = false)
    private String programa;

    @Column(name = "latitud", nullable = false)
    private Double latitud;

    @Column(name = "longitud", nullable = false)
    private Double longitud;

    @Column(name = "alcaldia", length = 100, nullable = false)
    private String alcaldia;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
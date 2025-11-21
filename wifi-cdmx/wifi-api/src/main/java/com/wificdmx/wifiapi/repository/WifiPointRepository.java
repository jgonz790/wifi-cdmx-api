package com.wificdmx.wifiapi.repository;

import com.wificdmx.wifiapi.model.WifiPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WifiPointRepository extends JpaRepository<WifiPoint, String> {
}
package com.disys.restapi.repository;

import com.disys.restapi.model.UsageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Read access to the usage_data table for the rest-api.
 */
@Repository
public interface UsageDataRepository extends JpaRepository<UsageData, UUID> {

    // Returns all hourly rows whose hour falls within [start, end] (inclusive).
    List<UsageData> findByHourBetween(LocalDateTime start, LocalDateTime end);
}
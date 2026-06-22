package com.disys.restapi.repository;

import com.disys.restapi.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Read access to the percentage table for the rest-api.
 */
@Repository
public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentage, UUID> {

    // Returns the row with the latest hour, i.e. the current percentages (null if empty).
    CurrentPercentage findTopByOrderByHourDesc();
}
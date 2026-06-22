package com.disys.percentage_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for Percentage rows; CRUD methods come from JpaRepository.
 */
@Repository
public interface PercentageRepository extends JpaRepository<Percentage, UUID> {
    // Finds the percentage row for a given hour, so updates overwrite instead of duplicating.
    Optional<Percentage> findByHour(LocalDateTime hour);
}

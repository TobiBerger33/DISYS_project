package com.disys.percentage_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PercentageRepository extends JpaRepository<Percentage, UUID> {
    Optional<Percentage> findByHour(LocalDateTime hour);
}

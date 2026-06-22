package com.disys.community_producer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for usage_data rows. The CRUD methods (save, findById,
 * ...) are provided automatically by JpaRepository.
 */
@Repository
public interface UsageRepository extends JpaRepository<UsageData, UUID> {

    // Finds the row for a specific hour (e.g. 2025-01-10T14:00:00); empty if none yet.
    // Spring derives the query automatically from the method name.
    Optional<UsageData> findByHour(LocalDateTime hour);
}

package com.disys.community_producer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageRepository extends JpaRepository<UsageData, UUID> {

    // Sucht den Eintrag für eine bestimmte Stunde (z.B. 2025-01-10T14:00:00)
    Optional<UsageData> findByHour(LocalDateTime hour);
}

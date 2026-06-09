package com.disys.restapi.repository;

import com.disys.restapi.model.UsageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsageDataRepository extends JpaRepository<UsageData, UUID> {

    List<UsageData> findByHourBetween(LocalDateTime start, LocalDateTime end);
}
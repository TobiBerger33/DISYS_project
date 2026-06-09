package com.disys.restapi.repository;

import com.disys.restapi.model.CurrentPercentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CurrentPercentageRepository extends JpaRepository<CurrentPercentage, UUID> {

    CurrentPercentage findTopByOrderByHourDesc();
}
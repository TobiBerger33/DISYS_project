package com.disys.restapi.controller;

import com.disys.restapi.model.CurrentPercentage;
import com.disys.restapi.model.UsageData;
import com.disys.restapi.repository.CurrentPercentageRepository;
import com.disys.restapi.repository.UsageDataRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final CurrentPercentageRepository percentageRepository;
    private final UsageDataRepository usageDataRepository;

    public EnergyController(CurrentPercentageRepository percentageRepository,
                            UsageDataRepository usageDataRepository) {
        this.percentageRepository = percentageRepository;
        this.usageDataRepository = usageDataRepository;
    }

    @GetMapping("/current")
    public CurrentPercentage getCurrent() {
        return percentageRepository.findTopByOrderByHourDesc();
    }

    @GetMapping("/historical")
    public List<UsageData> getHistorical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return usageDataRepository.findByHourBetween(start, end);
    }
}
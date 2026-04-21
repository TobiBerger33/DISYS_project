package com.disys.restapi.controller;

import com.disys.restapi.model.CurrentPercentage;
import com.disys.restapi.model.UsageData;
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

    private static final List<UsageData> SAMPLE_DATA = List.of(
            new UsageData(LocalDateTime.of(2025, 1, 1, 10, 0), 10.5, 9.8, 0.5),
            new UsageData(LocalDateTime.of(2025, 1, 13, 11, 0), 12.3, 11.5, 1.2),
            new UsageData(LocalDateTime.of(2025, 1, 17, 12, 0), 15.0, 14.0, 2.0),
            new UsageData(LocalDateTime.of(2025, 1, 10, 13, 0), 15.015, 14.033, 2.049),
            new UsageData(LocalDateTime.of(2025, 1, 19, 14, 0), 18.05, 18.05, 1.076),
            new UsageData(LocalDateTime.of(2025, 1, 14, 15, 0), 14.2, 13.5, 1.8),
            new UsageData(LocalDateTime.of(2025, 1, 10, 16, 0), 11.0, 10.2, 2.5),
            new UsageData(LocalDateTime.of(2025, 1, 21, 17, 0), 8.5, 8.0, 3.1)
    );

    @GetMapping("/current")
    public CurrentPercentage getCurrent() {
        return new CurrentPercentage(
                LocalDateTime.of(2025, 1, 10, 14, 0),
                100.0,
                5.63
        );
    }

    @GetMapping("/historical")
    public List<UsageData> getHistorical(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return SAMPLE_DATA.stream()
                .filter(d -> !d.getHour().isBefore(start) && !d.getHour().isAfter(end))
                .toList();
    }
}

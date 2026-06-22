package com.disys.community_producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

/**
 * Fetches the current solar radiation for Vienna from the free open-meteo API
 * and turns it into a 0..1 "sun factor". The result is cached for 5 minutes so
 * the fast production loop does not hammer the external API.
 */
@Service
public class WeatherService {

    // Radiation (W/m^2) treated as full sun; readings are normalized against this.
    private static final double MAX_RADIATION = 1000.0;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    // When the cached value was last fetched; null until the first call.
    private LocalDateTime cachedAt;
    // Last computed sun factor, reused while the cache is still fresh.
    private double cached;
    // open-meteo endpoint for Vienna (lat 48.2, lon 16.37), current shortwave radiation.
    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=48.2&longitude=16.37&current=shortwave_radiation";

    /**
     * @return current sun intensity as 0.0 (dark) .. 1.0 (full sun)
     */
    public double getSunFactor() throws IOException, InterruptedException {
        // Only call the API if we have no value yet or the cache is older than 5 minutes.
        if (cachedAt == null || cachedAt.isBefore(LocalDateTime.now().minusMinutes(5))) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            cachedAt = LocalDateTime.now();
            // Drill into the JSON: { "current": { "shortwave_radiation": <number> } }
            JsonNode jsonNode = mapper.readTree(response.body());
            double radiation = jsonNode.get("current").get("shortwave_radiation").asDouble();
            // Normalize to 0..1 and cap at 1.0 so very bright readings don't overshoot.
            cached = Math.min(1.0, radiation / MAX_RADIATION);
        }

        return cached;
    }
}

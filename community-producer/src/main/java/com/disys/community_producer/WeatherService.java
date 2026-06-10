package com.disys.community_producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class WeatherService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private LocalDateTime cachedAt;
    private double cached;
    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=48.2&longitude=16.37&current=cloud_cover";

    public double getSunFactor() throws IOException, InterruptedException {
        if (cachedAt == null || cachedAt.isBefore(LocalDateTime.now().minusMinutes(5))) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            cachedAt = LocalDateTime.now();
            JsonNode jsonNode = mapper.readTree(response.body());
            double cloudCover = jsonNode.get("current").get("cloud_cover").asDouble();
            cached = 1.0 - cloudCover / 100;
        }

        return cached;
    }
}

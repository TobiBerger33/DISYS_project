package com.energycommunity.javafxgui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Small HTTP client for the rest-api. Returns the raw JSON response body as a
 * String; the caller (HelloController) is responsible for parsing it.
 */
public class ApiClient {

    // Base address of the running rest-api.
    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    /** GET /energy/current and return the raw JSON body. */
    public String fetchCurrent() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/energy/current"))
                .GET()
                .build();
        return client.send(request,
                HttpResponse.BodyHandlers.ofString()).body();
    }

    /** GET /energy/historical for the given ISO date-time range; returns raw JSON. */
    public String fetchHistorical(String start, String end) throws Exception {
        String url = BASE_URL + "/energy/historical?start=" + start + "&end=" + end;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return client.send(request,
                HttpResponse.BodyHandlers.ofString()).body();
    }
}
package com.energycommunity.javafxgui;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client = HttpClient.newHttpClient();

    public String fetchCurrent() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/energy/current"))
                .GET()
                .build();
        return client.send(request,
                HttpResponse.BodyHandlers.ofString()).body();
    }

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
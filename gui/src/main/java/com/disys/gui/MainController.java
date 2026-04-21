package com.disys.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML private Label currentHourLabel;
    @FXML private Label communityDepletedLabel;
    @FXML private Label gridPortionLabel;
    @FXML private Label statusLabel;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<Map<String, Object>> historicalTable;
    @FXML private TableColumn<Map<String, Object>, String> hourColumn;
    @FXML private TableColumn<Map<String, Object>, Number> producedColumn;
    @FXML private TableColumn<Map<String, Object>, Number> usedColumn;
    @FXML private TableColumn<Map<String, Object>, Number> gridColumn;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String BASE_URL = "http://localhost:8080/energy";

    @FXML
    public void initialize() {
        hourColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("hour"))));
        producedColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("communityProduced")));
        usedColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("communityUsed")));
        gridColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("gridUsed")));

        startDatePicker.setValue(LocalDate.of(2025, 1, 10));
        endDatePicker.setValue(LocalDate.of(2025, 1, 10));
    }

    @FXML
    private void onFetchCurrent() {
        statusLabel.setText("Fetching current data...");

        Thread.startVirtualThread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/current"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Map<String, Object> data = mapper.readValue(response.body(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    currentHourLabel.setText(String.valueOf(data.get("hour")));
                    communityDepletedLabel.setText(data.get("communityDepleted") + "%");
                    gridPortionLabel.setText(data.get("gridPortion") + "%");
                    statusLabel.setText("Current data loaded.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        });
    }

    @FXML
    private void onFetchHistorical() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            statusLabel.setText("Please select both start and end dates.");
            return;
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        String url = BASE_URL + "/historical?start=" +
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "&end=" + end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        statusLabel.setText("Fetching historical data...");

        Thread.startVirtualThread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                List<Map<String, Object>> data = mapper.readValue(response.body(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    historicalTable.setItems(FXCollections.observableArrayList(data));
                    statusLabel.setText("Loaded " + data.size() + " records.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        });
    }
}

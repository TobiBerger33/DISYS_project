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

/**
 * Controller behind main.fxml. Fetches data from the rest-api over HTTP and
 * fills the labels and the historical table. All network calls run on a
 * background thread and push their results back onto the JavaFX thread.
 */
public class MainController {

    // @FXML fields are injected by the FXMLLoader from elements with a matching fx:id.
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

    // Called automatically by the FXMLLoader after the @FXML fields are injected.
    @FXML
    public void initialize() {
        // Tell each table column which key of the row's Map to display.
        hourColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("hour"))));
        producedColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("communityProduced")));
        usedColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("communityUsed")));
        gridColumn.setCellValueFactory(data ->
                new SimpleDoubleProperty((Double) data.getValue().get("gridUsed")));

        // Pre-fill the date pickers with a day that has sample data.
        startDatePicker.setValue(LocalDate.of(2025, 1, 10));
        endDatePicker.setValue(LocalDate.of(2025, 1, 10));
    }

    // Wired to the "fetch current" button in the FXML.
    @FXML
    private void onFetchCurrent() {
        statusLabel.setText("Fetching current data...");

        // Do the blocking HTTP call off the UI thread so the window stays responsive.
        Thread.startVirtualThread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/current"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                // Parse the JSON object into a generic key -> value map.
                Map<String, Object> data = mapper.readValue(response.body(), new TypeReference<>() {});

                // UI updates must happen on the JavaFX thread -> hand them to Platform.runLater.
                Platform.runLater(() -> {
                    currentHourLabel.setText(String.valueOf(data.get("hour")));
                    communityDepletedLabel.setText(data.get("communityDepleted") + "%");
                    gridPortionLabel.setText(data.get("gridPortion") + "%");
                    statusLabel.setText("Current data loaded.");
                });
            } catch (Exception e) {
                // Report failures (e.g. API down) in the status label, again on the UI thread.
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        });
    }

    // Wired to the "fetch historical" button in the FXML.
    @FXML
    private void onFetchHistorical() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Both dates are required to build the query range.
        if (startDate == null || endDate == null) {
            statusLabel.setText("Please select both start and end dates.");
            return;
        }

        // Expand the picked dates to cover the whole day: 00:00:00 .. 23:59:59.
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // Build the query URL with ISO date-time params the rest-api expects.
        String url = BASE_URL + "/historical?start=" +
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                "&end=" + end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        statusLabel.setText("Fetching historical data...");

        // Same pattern as onFetchCurrent: HTTP off-thread, UI updates via runLater.
        Thread.startVirtualThread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                // Historical endpoint returns a JSON array -> list of row maps.
                List<Map<String, Object>> data = mapper.readValue(response.body(), new TypeReference<>() {});

                Platform.runLater(() -> {
                    // Replace the table contents with the freshly loaded rows.
                    historicalTable.setItems(FXCollections.observableArrayList(data));
                    statusLabel.setText("Loaded " + data.size() + " records.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        });
    }
}

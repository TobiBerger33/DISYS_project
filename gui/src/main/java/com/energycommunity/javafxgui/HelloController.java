package com.energycommunity.javafxgui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

/**
 * Controller behind hello-view.fxml. Shows the current percentages and the
 * summed totals over a chosen date range, using ApiClient to talk to the rest-api.
 * Note: unlike MainController, the calls here run on the JavaFX thread directly.
 */
public class HelloController {

    // @FXML fields are injected by the FXMLLoader from elements with a matching fx:id.
    @FXML private Label communityPoolLabel;
    @FXML private Label gridPortionLabel;
    @FXML private Label currentErrorLabel;
    @FXML private Label producedLabel;
    @FXML private Label usedLabel;
    @FXML private Label gridUsedLabel;
    @FXML private Label histErrorLabel;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;

    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Wired to the "refresh" button: load and display the current percentages.
    @FXML
    private void handleRefresh() {
        currentErrorLabel.setText(""); // clear any previous error
        try {
            String json = apiClient.fetchCurrent();
            // Read the two percentage fields straight out of the JSON object.
            JsonNode node = mapper.readTree(json);
            double depleted = node.get("communityDepleted").asDouble();
            double grid     = node.get("gridPortion").asDouble();
            communityPoolLabel.setText(String.format("%.2f%% used", depleted));
            gridPortionLabel.setText(String.format("%.2f%%", grid));
        } catch (Exception e) {
            // Most likely the rest-api isn't running or isn't reachable.
            currentErrorLabel.setText("Could not reach the API. Is it running?");
        }
    }

    // Wired to the "show data" button: sum the usage rows over the chosen range.
    @FXML
    private void handleShowData() {
        histErrorLabel.setText(""); // clear any previous error
        // Both dates are needed to build the range.
        if (startPicker.getValue() == null || endPicker.getValue() == null) {
            histErrorLabel.setText("Please select both a start and end date.");
            return;
        }
        // Turn the picked dates into the ISO date-time strings the API expects.
        String start = startPicker.getValue() + "T00:00:00";
        String end   = endPicker.getValue()   + "T00:00:00";
        try {
            String json = apiClient.fetchHistorical(start, end);
            JsonNode array = mapper.readTree(json);

            // Accumulate the totals across every hourly row in the response.
            double totalProduced = 0;
            double totalUsed = 0;
            double totalGrid = 0;

            for (JsonNode node : array) {
                totalProduced += node.get("communityProduced").asDouble();
                totalUsed     += node.get("communityUsed").asDouble();
                totalGrid     += node.get("gridUsed").asDouble();
            }

            producedLabel.setText(String.format("%.3f kWh", totalProduced));
            usedLabel.setText(String.format("%.3f kWh", totalUsed));
            gridUsedLabel.setText(String.format("%.3f kWh", totalGrid));

            // All zero usually means the range simply has no data.
            if (totalProduced == 0 && totalUsed == 0 && totalGrid == 0) {
                histErrorLabel.setText("No data found for the selected range.");
            }

        } catch (Exception e) {
            histErrorLabel.setText("Could not load historical data.");
        }
    }
}

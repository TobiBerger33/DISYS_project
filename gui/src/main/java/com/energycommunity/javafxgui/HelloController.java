package com.energycommunity.javafxgui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

public class HelloController {

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

    @FXML
    private void handleRefresh() {
        currentErrorLabel.setText("");
        try {
            String json = apiClient.fetchCurrent();
            JsonNode node = mapper.readTree(json);
            double depleted = node.get("communityDepleted").asDouble();
            double grid     = node.get("gridPortion").asDouble();
            communityPoolLabel.setText(String.format("%.2f%% used", depleted));
            gridPortionLabel.setText(String.format("%.2f%%", grid));
        } catch (Exception e) {
            currentErrorLabel.setText("Could not reach the API. Is it running?");
        }
    }

    @FXML
    private void handleShowData() {
        histErrorLabel.setText("");
        if (startPicker.getValue() == null || endPicker.getValue() == null) {
            histErrorLabel.setText("Please select both a start and end date.");
            return;
        }
        String start = startPicker.getValue() + "T00:00:00";
        String end   = endPicker.getValue()   + "T00:00:00";
        try {
            String json = apiClient.fetchHistorical(start, end);
            JsonNode array = mapper.readTree(json);

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

            if (totalProduced == 0 && totalUsed == 0 && totalGrid == 0) {
                histErrorLabel.setText("No data found for the selected range.");
            }

        } catch (Exception e) {
            histErrorLabel.setText("Could not load historical data.");
        }
    }
}

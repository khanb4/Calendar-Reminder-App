package com.example.calendarreminderapp.controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

public class CalenderController {
    @FXML
    private AnchorPane root;

    @FXML
    private BorderPane borderPane;

    @FXML
    private GridPane calendarGridPane;

    @FXML
    private Label monthLabel;

    @FXML
    private Button backToDashboard;

    @FXML
    public void initialize() {
        YearMonth currentYearMonth = YearMonth.now();
        fillCalendar(currentYearMonth);

        //sets the month label to the current month.
        monthLabel.setText(currentYearMonth.getMonth().toString());

        //sets up the button to return to dashboard
        backToDashboard.setOnAction(e -> switchToDashboard());
    }

    private void switchToDashboard(){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/calendarreminderapp/dashboard.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) backToDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void fillCalendar(YearMonth yearMonth) {
        calendarGridPane.getChildren().clear();
        LocalDate firstDay = yearMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue();

        int daysInMonth = yearMonth.lengthOfMonth();
        int col = dayOfWeek % 7;
        int row = 0;

        for (int day = 1; day <= daysInMonth; day++) {

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("calender-view");

            // Add day to grid in correct position
            calendarGridPane.add(dayLabel, col, row);

            // Move to next column
            col++;

            // If we reach end of week, wrap to next row
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }
}

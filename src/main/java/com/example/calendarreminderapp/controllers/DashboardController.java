package com.example.calendarreminderapp.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button reminderButton;

    @FXML
    private Button calendarButton;

    private String currentUser;

    // Called from LoginController after successful login
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser + "!");
        }
    }

    @FXML
    public void initialize() {
        if (calendarButton != null) {
            calendarButton.setOnAction(e -> handleCalendarView());
        }
    }

    @FXML
    private void handleCalendarView() {
        openCalendarView();
    }

    @FXML
    private void handleReminderView() {
        System.out.println("Reminder view not implemented yet.");
    }

    private void openCalendarView() {
        // DEBUG PRINT — MUST SHOW WHEN BUTTON IS CLICKED
        System.out.println("DEBUG: Opening calendar view for user: " + currentUser);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/calendarreminderapp/calendar-view.fxml")
            );

            Parent root = loader.load();

            // Pass current user into CalendarController
            CalendarController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Switch scenes
            Stage stage = (Stage) calendarButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.calendarreminderapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HelpController {

    @FXML
    private Button backButton;

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}
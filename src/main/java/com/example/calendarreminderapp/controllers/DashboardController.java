package com.example.calendarreminderapp.controllers;

import com.example.calendarreminderapp.database.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.concurrent.ExecutionException;

public class DashboardController {
    @FXML
    private Button calenderButton;

    @FXML
    private AnchorPane anchorPane;

    public void initialize()
    {
        calenderButton.setOnAction(e -> switchToCalender());
    }

    public void switchToCalender()
    {
        try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/calendarreminderapp/calender.fxml")
        );
            Parent root = loader.load();
            Stage stage = (Stage) calenderButton.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
    } catch (IOException ex) {
        ex.printStackTrace();
    }
    }



}

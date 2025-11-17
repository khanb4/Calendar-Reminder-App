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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private UserRepository userRepository;

    @FXML
    public void initialize() {
        try {
            userRepository = new UserRepository();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("❌ Error initializing database");
            return;
        }

        loginButton.setOnAction(e -> login());
        registerButton.setOnAction(e -> switchToRegister());
    }

    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("⚠ Please fill in all fields");
            return;
        }

        try {
            boolean valid = userRepository.validateUser(username, password);

            if (valid) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("✅ Login successful!");
                // TODO: navigate to your main calendar screen
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid username or password");
            }
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("⚠ Error connecting to database");
        }
    }

    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/calendarreminderapp/register.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

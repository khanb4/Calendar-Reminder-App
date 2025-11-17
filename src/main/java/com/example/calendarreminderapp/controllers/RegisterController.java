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

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

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

        registerButton.setOnAction(e -> registerUser());
        backToLoginButton.setOnAction(e -> goBackToLogin());
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setText("❌ Please fill in all fields");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("❌ Passwords do not match");
            return;
        }

        try {
            userRepository.createUser(username, password);

            messageLabel.setText("✅ Registration successful!");
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            messageLabel.setText("❌ Registration failed!");
        }
    }

    private void goBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/calendarreminderapp/login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

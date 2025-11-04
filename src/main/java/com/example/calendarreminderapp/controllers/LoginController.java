package com.example.calendarreminderapp.controllers;

import com.example.calendarreminderapp.database.Database;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> login());
        registerButton.setOnAction(e -> switchToRegister());
    }

    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("⚠ Please fill in all fields");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            if (conn == null) {
                messageLabel.setText("❌ Cannot connect to database");
                return;
            }

            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("✅ Login successful!");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid username or password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("⚠ Error connecting to database");
        }
    }

    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendarreminderapp/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

package controllers;

import database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> registerUser());
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("❌ Please enter username and password");
            return;
        }

        // Get DB connection
        try (Connection conn = Database.getConnection()) {
            if (conn == null) {
                messageLabel.setText("❌ Error connecting to database");
                return;
            }

            // Insert user
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            messageLabel.setText("✅ Registration successful!");
            usernameField.clear();
            passwordField.clear();

        } catch (SQLException ex) {
            ex.printStackTrace();
            messageLabel.setText("❌ Registration failed!");
        }
    }
}

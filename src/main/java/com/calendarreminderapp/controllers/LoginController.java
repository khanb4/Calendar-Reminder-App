package com.calendarreminderapp.controllers;

import com.calendarreminderapp.database.UserRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LoginController {

    @FXML private ImageView bgImage;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    private UserRepository userRepository;

    @FXML
    public void initialize() {
        try {
            userRepository = new UserRepository();
        } catch (IOException e) {
            e.printStackTrace();
            if (messageLabel != null) {
                messageLabel.setText("Error connecting to database.");
            }
        }

        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> switchToRegister());

        // Background image resize bind

        bgImage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                bgImage.fitWidthProperty().bind(newScene.widthProperty());
                bgImage.fitHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            boolean ok = userRepository.validateUser(username, password);
            if (ok) {
                switchToCalendar(username);
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            messageLabel.setText("Login failed. Please try again.");
        }
    }

    private void switchToCalendar(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/calendarreminderapp/calendar-view.fxml")
            );
            Parent root = loader.load();

            CalendarController controller = loader.getController();

            //  FXML loads BEFORE controller logic runs
            Platform.runLater(() -> controller.setCurrentUser(username));

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 650));
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            messageLabel.setText("Unable to open calendar.");
        }
    }

    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/calendarreminderapp/register.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 420, 420));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

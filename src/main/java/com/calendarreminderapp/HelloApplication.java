package com.calendarreminderapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/calendarreminderapp/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Calendar Reminder App - Login");
        stage.setScene(scene);

        // ✅ Polished window look
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.centerOnScreen();

        // Optional full-screen mode:
        // stage.setFullScreen(true);
        // stage.setFullScreenExitHint("");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

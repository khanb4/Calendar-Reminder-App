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

        // NEW window look
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.centerOnScreen();



        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

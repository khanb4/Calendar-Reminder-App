module com.example.calendarreminderapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.calendarreminderapp to javafx.fxml;
    opens com.example.calendarreminderapp.controllers to javafx.fxml;
    opens com.example.calendarreminderapp.database to javafx.fxml;

    exports com.example.calendarreminderapp;
    exports com.example.calendarreminderapp.controllers;
}

module com.example.calendarreminderapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.calendarreminderapp to javafx.fxml;
    opens controllers to javafx.fxml;
    exports com.example.calendarreminderapp;
    exports controllers;
}

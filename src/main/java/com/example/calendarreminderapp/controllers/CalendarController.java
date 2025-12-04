package com.example.calendarreminderapp.controllers;

import com.example.calendarreminderapp.database.Reminder;
import com.example.calendarreminderapp.database.ReminderRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalendarController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Label selectedDateLabel;

    // NEW unified upcoming reminders column
    @FXML private ListView<String> upcomingRemindersList;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> ampmCombo;
    @FXML private Button addReminderButton;
    @FXML private Button backButton;

    private ReminderRepository reminderRepository;
    private String currentUser;
    private YearMonth currentYearMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    // -------------------- Public API --------------------

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        buildCalendar();
        loadUpcomingReminders();
    }

    // -------------------- Initialize --------------------

    @FXML
    public void initialize() {
        try {
            reminderRepository = new ReminderRepository();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setupTimeDropdowns();

        updateMonthLabel();
        buildCalendar();
        updateSelectedDateLabel();
        loadUpcomingReminders();

        addReminderButton.setOnAction(e -> handleAddReminder());
        backButton.setOnAction(e -> handleBack());
    }

    private void setupTimeDropdowns() {
        if (hourCombo != null) {
            hourCombo.getItems().clear();
            for (int i = 1; i <= 12; i++) hourCombo.getItems().add(String.valueOf(i));
        }

        if (minuteCombo != null) {
            minuteCombo.getItems().clear();
            for (int i = 0; i < 60; i++)
                minuteCombo.getItems().add(String.format("%02d", i));
            minuteCombo.setPromptText("Min");
        }

        if (ampmCombo != null) {
            ampmCombo.getItems().clear();
            ampmCombo.getItems().addAll("AM", "PM");
            ampmCombo.getSelectionModel().select("AM");
        }
    }

    // -------------------- Load Upcoming Reminders --------------------

    private void loadUpcomingReminders() {
        if (reminderRepository == null || currentUser == null) return;

        try {
            List<Reminder> reminders =
                    reminderRepository.getUpcomingReminders(currentUser);

            upcomingRemindersList.getItems().clear();

            for (Reminder r : reminders) {
                String text = r.getDate() + "  " + r.getTime() + " — " + r.getTitle();
                if (r.getDescription() != null && !r.getDescription().isBlank()) {
                    text += "\n" + r.getDescription();
                }
                upcomingRemindersList.getItems().add(text);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- Navigation --------------------

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        buildCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        buildCalendar();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/calendarreminderapp/dashboard.fxml")
            );
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------- Day Click & Add Reminder --------------------

    private void handleDayClicked(LocalDate date) {
        selectedDate = date;
        updateSelectedDateLabel();
        highlightSelectedDay();
    }

    private void handleAddReminder() {
        if (currentUser == null) {
            System.err.println("User not set");
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String ampm = ampmCombo.getValue();

        if (title.isEmpty()) {
            System.err.println("Title required");
            return;
        }
        if (hour == null || minute == null || ampm == null) {
            System.err.println("Time required");
            return;
        }

        String time = hour + ":" + minute + " " + ampm;
        LocalDate date = (selectedDate != null) ? selectedDate : LocalDate.now();

        try {
            reminderRepository.addReminder(currentUser, date, title, description, time);

            titleField.clear();
            descriptionArea.clear();
            hourCombo.getSelectionModel().clearSelection();
            minuteCombo.getSelectionModel().clearSelection();
            minuteCombo.setPromptText("Min");
            ampmCombo.getSelectionModel().select("AM");

            buildCalendar();
            loadUpcomingReminders();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // -------------------- Calendar Grid --------------------

    private void buildCalendar() {
        calendarGrid.getChildren().clear();

        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };

        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i].name().substring(0, 3));
            label.getStyleClass().add("calendar-header");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            calendarGrid.add(label, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int length = currentYearMonth.lengthOfMonth();
        int firstCol = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7;

        int day = 1;
        int row = 1;

        while (day <= length) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < firstCol) continue;
                if (day > length) break;

                LocalDate date = currentYearMonth.atDay(day);
                VBox cell = createDayCell(date);
                calendarGrid.add(cell, col, row);
                day++;
            }
            row++;
        }

        updateMonthLabel();
        highlightSelectedDay();
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell");
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setPadding(new Insets(6));
        cell.setSpacing(4);

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNum);

        try {
            List<Reminder> reminders = reminderRepository.getRemindersForDate(currentUser, date);
            if (!reminders.isEmpty()) {
                Label dot = new Label("• " + reminders.size() + " reminder(s)");
                dot.getStyleClass().add("calendar-reminder-icon");
                cell.getChildren().add(dot);
            }
        } catch (Exception ignored) {}

        cell.setOnMouseClicked(e -> handleDayClicked(date));
        return cell;
    }

    private void highlightSelectedDay() {
        for (javafx.scene.Node node : calendarGrid.getChildren()) {
            node.getStyleClass().remove("calendar-day-selected");

            if (node instanceof VBox cell) {
                Integer rowIdx = GridPane.getRowIndex(cell);
                if (rowIdx == null || rowIdx == 0) continue;

                for (javafx.scene.Node child : cell.getChildren()) {
                    if (child instanceof Label label) {
                        try {
                            int day = Integer.parseInt(label.getText());
                            LocalDate date = currentYearMonth.atDay(day);
                            if (date.equals(selectedDate)) {
                                cell.getStyleClass().add("calendar-day-selected");
                            }
                            break;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
    }

    private void updateMonthLabel() {
        String monthName = currentYearMonth.getMonth().name().toLowerCase();
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        monthYearLabel.setText(monthName + " " + currentYearMonth.getYear());
    }

    private void updateSelectedDateLabel() {
        if (selectedDateLabel != null && selectedDate != null) {
            selectedDateLabel.setText("Selected: " + selectedDate.toString());
        }
    }
}
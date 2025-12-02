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

    @FXML
    private Label monthYearLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label selectedDateLabel;

    @FXML
    private ListView<Reminder> reminderListView;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ComboBox<String> hourCombo;

    @FXML
    private ComboBox<String> minuteCombo;

    @FXML
    private ComboBox<String> ampmCombo;

    @FXML
    private Button addReminderButton;

    @FXML
    private Button backButton;

    private ReminderRepository reminderRepository;

    private String currentUser;
    private YearMonth currentYearMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        refreshReminders();
    }

    @FXML
    public void initialize() {
        try {
            reminderRepository = new ReminderRepository();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        setupTimeDropdowns();
        configureReminderListView();

        updateMonthLabel();
        buildCalendar();
        updateSelectedDateLabel();

        addReminderButton.setOnAction(e -> handleAddReminder());
        backButton.setOnAction(e -> handleBack());
    }

    private void setupTimeDropdowns() {
        if (hourCombo != null) {
            hourCombo.getItems().clear();
            for (int i = 1; i <= 12; i++) {
                hourCombo.getItems().add(String.valueOf(i));  // 1–12
            }
        }

        if (minuteCombo != null) {
            minuteCombo.getItems().clear();
            for (int i = 0; i < 60; i++) {                   // 00–59
                minuteCombo.getItems().add(String.format("%02d", i));
            }
            minuteCombo.setPromptText("Min");
        }

        if (ampmCombo != null) {
            ampmCombo.getItems().clear();
            ampmCombo.getItems().addAll("AM", "PM");
            ampmCombo.getSelectionModel().select("AM");
        }
    }

    /** Show date, time, and description in the big box under the calendar */
    private void configureReminderListView() {
        // allow variable-height rows for multi-line text
        reminderListView.setFixedCellSize(-1);

        reminderListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Reminder item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String dateStr = (selectedDate != null)
                            ? selectedDate.toString()
                            : "";

                    String timeStr = (item.getTime() != null && !item.getTime().isBlank())
                            ? item.getTime()
                            : "";

                    String titleStr = (item.getTitle() != null)
                            ? item.getTitle()
                            : "";

                    String descStr = (item.getDescription() != null)
                            ? item.getDescription()
                            : "";

                    StringBuilder sb = new StringBuilder();

                    // First line: date + time + title
                    if (!dateStr.isEmpty()) {
                        sb.append(dateStr);
                    }
                    if (!timeStr.isEmpty()) {
                        if (sb.length() > 0) sb.append("  ");
                        sb.append(timeStr);
                    }
                    if (!titleStr.isBlank()) {
                        if (sb.length() > 0) sb.append("  –  ");
                        sb.append(titleStr);
                    }

                    // Second line: description
                    if (!descStr.isBlank()) {
                        sb.append("\n").append(descStr);
                    }

                    setText(sb.toString());
                    setWrapText(true);
                }
            }
        });
    }

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

    // Called when you click on a date cell
    private void handleDayClicked(LocalDate date) {
        selectedDate = date;
        updateSelectedDateLabel();   // e.g. "Selected: 2025-12-01"
        refreshReminders();          // reload list for this date
        highlightSelectedDay();      // visually highlight the cell
    }

    private void handleAddReminder() {
        if (currentUser == null || currentUser.isBlank()) {
            System.err.println("User not set");
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String ampm = ampmCombo.getValue();

        if (title.isEmpty()) {
            System.err.println("Title is required");
            return;
        }

        if (hour == null || minute == null || ampm == null) {
            System.err.println("Time is required");
            return;
        }

        String time = hour + ":" + minute + " " + ampm;

        try {
            // save to Firestore for the selected date
            reminderRepository.addReminder(currentUser, selectedDate, title, description, time);

            // clear inputs
            titleField.clear();
            descriptionArea.clear();
            hourCombo.getSelectionModel().clearSelection();
            minuteCombo.getSelectionModel().clearSelection();
            minuteCombo.setPromptText("Min");
            ampmCombo.getSelectionModel().select("AM");

            // refresh list and calendar
            refreshReminders();
            buildCalendar();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void refreshReminders() {
        if (reminderRepository == null || currentUser == null) {
            return;
        }

        try {
            List<Reminder> reminders = reminderRepository.getRemindersForDate(currentUser, selectedDate);
            reminderListView.getItems().setAll(reminders);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();

        DayOfWeek[] days = {
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
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
        int monthLength = currentYearMonth.lengthOfMonth();
        int firstDayColumn = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7; // Monday=0

        int day = 1;
        int row = 1;

        while (day <= monthLength) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < firstDayColumn) {
                    continue;
                }
                if (day > monthLength) {
                    break;
                }

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
        cell.setPadding(new Insets(4));
        cell.setSpacing(4);

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        // Show icon + count of reminders for that day (for this user)
        try {
            if (currentUser != null) {
                List<Reminder> reminders = reminderRepository.getRemindersForDate(currentUser, date);
                if (!reminders.isEmpty()) {
                    int count = reminders.size();
                    String plural = (count == 1) ? " reminder" : " reminders";
                    Label iconLabel = new Label("🔔 " + count + plural);
                    iconLabel.getStyleClass().add("calendar-reminder-icon");
                    cell.getChildren().add(iconLabel);
                }
            }
        } catch (Exception ignored) {
        }

        // Click event: select this date
        cell.setOnMouseClicked(e -> handleDayClicked(date));

        return cell;
    }


    // Highlight the selected day's cell
    private void highlightSelectedDay() {
        for (javafx.scene.Node node : calendarGrid.getChildren()) {
            // Remove old highlight
            node.getStyleClass().remove("calendar-day-selected");

            if (node instanceof VBox cell) {
                Integer rowIndex = GridPane.getRowIndex(cell);
                if (rowIndex == null || rowIndex == 0) {
                    // Skip header row (Mon/Tue/...)
                    continue;
                }

                for (javafx.scene.Node child : cell.getChildren()) {
                    if (child instanceof Label label) {
                        String text = label.getText();
                        try {
                            int day = Integer.parseInt(text);
                            LocalDate date = currentYearMonth.atDay(day);
                            if (date.equals(selectedDate)) {
                                cell.getStyleClass().add("calendar-day-selected");
                            }
                            break;
                        } catch (NumberFormatException ignored) {
                        }
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
        selectedDateLabel.setText("Selected: " + selectedDate.toString());
    }
}

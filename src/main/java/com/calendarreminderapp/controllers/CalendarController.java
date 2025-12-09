package com.calendarreminderapp.controllers;

import com.calendarreminderapp.database.Reminder;
import com.calendarreminderapp.database.ReminderRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CalendarController {

    // -------------------- UI Elements --------------------
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Label selectedDateLabel;

    @FXML private ListView<String> dayRemindersList;       // Selected-day reminders
    @FXML private ListView<String> upcomingRemindersList;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> hourCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> ampmCombo;
    @FXML private Button addReminderButton;
    @FXML private Button backButton;

    @FXML private Button editReminderButton;
    @FXML private Button deleteReminderButton;

    // -------------------- Data --------------------
    private ReminderRepository reminderRepository;
    private String currentUser;

    private YearMonth currentYearMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    private List<Reminder> upcomingReminders = new ArrayList<>();
    private List<Reminder> dayReminders = new ArrayList<>();

    private Reminder selectedReminder;

    // -------------------- Public API --------------------
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        buildCalendar();
        loadUpcomingReminders();
        updateDayReminders();
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

        buildCalendar();
        updateSelectedDateLabel();
        loadUpcomingReminders();
        updateDayReminders();

        if (addReminderButton != null) {
            addReminderButton.setOnAction(e -> handleAddReminder());
        }
        if (backButton != null) {
            backButton.setOnAction(e -> handleLogout());
        }

        if (editReminderButton != null) {
            editReminderButton.setDisable(true);
        }
        if (deleteReminderButton != null) {
            deleteReminderButton.setDisable(true);
        }

        // Clicking upcoming reminders
        if (upcomingRemindersList != null) {
            upcomingRemindersList.getSelectionModel()
                    .selectedIndexProperty()
                    .addListener((obs, oldVal, newVal) -> {
                        if (newVal == null ||
                                newVal.intValue() < 0 ||
                                newVal.intValue() >= upcomingReminders.size()) {

                            selectedReminder = null;
                            if (editReminderButton != null) editReminderButton.setDisable(true);
                            if (deleteReminderButton != null) deleteReminderButton.setDisable(true);
                            return;
                        }

                        selectedReminder = upcomingReminders.get(newVal.intValue());
                        if (editReminderButton != null) editReminderButton.setDisable(false);
                        if (deleteReminderButton != null) deleteReminderButton.setDisable(false);
                        populateFormFromReminder(selectedReminder);
                    });
        }
    }

    // -------------------- Time Dropdowns --------------------
    private void setupTimeDropdowns() {
        if (hourCombo != null) {
            for (int i = 1; i <= 12; i++) hourCombo.getItems().add(String.valueOf(i));
            hourCombo.setPromptText("Hour");
        }

        if (minuteCombo != null) {
            for (int i = 0; i < 60; i++) minuteCombo.getItems().add(String.format("%02d", i));
            minuteCombo.setPromptText("Min");
        }

        if (ampmCombo != null) {
            ampmCombo.getItems().addAll("AM", "PM");
            ampmCombo.getSelectionModel().select("AM");
        }
    }

    // -------------------- Load Upcoming Reminders --------------------
    private void loadUpcomingReminders() {
        if (currentUser == null || reminderRepository == null) return;

        try {
            upcomingReminders = reminderRepository.getUpcomingReminders(currentUser);

            // Sort by date then time
            upcomingReminders.sort(Comparator
                    .comparing((Reminder r) -> LocalDate.parse(r.getDate()))
                    .thenComparing(Reminder::getTime));

            if (upcomingRemindersList != null) {
                upcomingRemindersList.getItems().clear();
                for (Reminder r : upcomingReminders) {
                    upcomingRemindersList.getItems().add(r.toString());
                }
            }

            if (editReminderButton != null) editReminderButton.setDisable(true);
            if (deleteReminderButton != null) deleteReminderButton.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- Update Day Reminders --------------------
    private void updateDayReminders() {
        if (currentUser == null || reminderRepository == null) return;

        try {
            dayReminders = reminderRepository.getRemindersForDate(currentUser, selectedDate);

            dayReminders.sort(Comparator.comparing(Reminder::getTime));

            if (dayRemindersList != null) {
                dayRemindersList.getItems().clear();
                for (Reminder r : dayReminders) {
                    String text = r.getTime() + " — " + r.getTitle();
                    if (r.getDescription() != null && !r.getDescription().isBlank()) {
                        text += "\n" + r.getDescription();
                    }
                    dayRemindersList.getItems().add(text);
                }
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
    private void handleLogout() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/calendarreminderapp/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------- Day Click --------------------
    private void handleDayClicked(LocalDate date) {
        selectedDate = date;
        updateSelectedDateLabel();
        highlightSelectedDay();
        updateDayReminders();
    }

    // -------------------- Add Reminder --------------------
    @FXML
    private void handleAddReminder() {
        if (currentUser == null || reminderRepository == null) return;

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String ampm = ampmCombo.getValue();

        if (title.isEmpty() || hour == null || minute == null || ampm == null) return;

        String time = hour + ":" + minute + " " + ampm;
        LocalDate date = selectedDate;

        try {
            reminderRepository.addReminder(currentUser, date, title, description, time);
            clearForm();
            buildCalendar();
            loadUpcomingReminders();
            updateDayReminders();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // -------------------- Edit Reminder --------------------
    @FXML
    private void handleEditReminder() {
        if (selectedReminder == null || reminderRepository == null) return;

        String newTitle = titleField.getText().trim();
        String newDesc = descriptionArea.getText().trim();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String ampm = ampmCombo.getValue();

        if (newTitle.isEmpty() || hour == null || minute == null || ampm == null) return;

        String newTime = hour + ":" + minute + " " + ampm;
        LocalDate newDate = selectedDate;

        try {
            reminderRepository.updateReminder(
                    currentUser,
                    selectedReminder.getDate(),
                    selectedReminder.getTime(),
                    selectedReminder.getTitle(),
                    newDate,
                    newTitle,
                    newDesc,
                    newTime
            );

            clearForm();
            buildCalendar();
            loadUpcomingReminders();
            updateDayReminders();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- Delete Reminder --------------------
    @FXML
    private void handleDeleteReminder() {
        if (selectedReminder == null || reminderRepository == null) return;

        try {
            reminderRepository.deleteReminder(
                    currentUser,
                    selectedReminder.getDate(),
                    selectedReminder.getTime(),
                    selectedReminder.getTitle()
            );

            clearForm();
            buildCalendar();
            loadUpcomingReminders();
            updateDayReminders();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- Form Helpers --------------------
    private void populateFormFromReminder(Reminder r) {
        if (r == null) return;

        titleField.setText(r.getTitle());
        descriptionArea.setText(r.getDescription());

        selectedDate = LocalDate.parse(r.getDate());
        updateSelectedDateLabel();
        highlightSelectedDay();

        try {
            String[] parts = r.getTime().split(" ");
            String[] hm = parts[0].split(":");

            hourCombo.getSelectionModel().select(hm[0]);
            minuteCombo.getSelectionModel().select(hm[1]);
            ampmCombo.getSelectionModel().select(parts[1]);

        } catch (Exception ignored) {}
    }

    private void clearForm() {
        if (titleField != null) titleField.clear();
        if (descriptionArea != null) descriptionArea.clear();

        if (hourCombo != null) hourCombo.getSelectionModel().clearSelection();
        if (minuteCombo != null) minuteCombo.getSelectionModel().clearSelection();
        if (ampmCombo != null) ampmCombo.getSelectionModel().select("AM");
    }

    // -------------------- Calendar Rendering --------------------
    private void buildCalendar() {
        if (calendarGrid == null) return;

        // Clear existing content & constraints
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // 7 columns (Mon–Sun) – each gets equal width and grows
        for (int col = 0; col < 7; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            cc.setPercentWidth(100.0 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // 7 rows (header + up to 6 weeks)
        for (int row = 0; row < 7; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            rc.setPercentHeight(100.0 / 7.0);
            calendarGrid.getRowConstraints().add(rc);
        }

        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        // Header row
        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i].name().substring(0, 3));
            label.getStyleClass().add("calendar-header");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setVgrow(label, Priority.ALWAYS);

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
        updateDayReminders();
    }

    // -------------------- Create Day Cell --------------------
    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell");
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setPadding(new Insets(6));
        cell.setSpacing(4);

        // Make the day cell resizable inside the GridPane
        cell.setMinSize(0, 0);
        cell.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setVgrow(cell, Priority.ALWAYS);

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNum);

        try {
            List<Reminder> reminders =
                    (reminderRepository != null)
                            ? reminderRepository.getRemindersForDate(currentUser, date)
                            : List.of();

            if (!reminders.isEmpty()) {
                Label dot = new Label("• " + reminders.size() + " reminder(s)");
                dot.getStyleClass().add("calendar-reminder-icon");
                cell.getChildren().add(dot);
            }
        } catch (Exception ignored) {}

        cell.setOnMouseClicked(e -> handleDayClicked(date));

        return cell;
    }

    // -------------------- Helpers --------------------
    private void highlightSelectedDay() {
        if (calendarGrid == null) return;

        for (var node : calendarGrid.getChildren()) {
            node.getStyleClass().remove("calendar-day-selected");

            if (node instanceof VBox cell) {
                Integer rowIdx = GridPane.getRowIndex(cell);
                if (rowIdx == null || rowIdx == 0) continue;

                for (var child : cell.getChildren()) {
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
        if (monthYearLabel == null) return;

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
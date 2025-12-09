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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CalendarController {

    // -------------------- View Mode --------------------
    private enum ViewMode {
        MONTH,
        WEEK,
        DAY
    }

    private ViewMode currentViewMode = ViewMode.MONTH;

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

    @FXML private Button monthViewButton;
    @FXML private Button weekViewButton;
    @FXML private Button dayViewButton;

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
        currentYearMonth = YearMonth.from(selectedDate);
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

        currentYearMonth = YearMonth.from(selectedDate);
        buildCalendar();
        updateSelectedDateLabel();
        loadUpcomingReminders();
        updateDayReminders();

        if (editReminderButton != null) editReminderButton.setDisable(true);
        if (deleteReminderButton != null) deleteReminderButton.setDisable(true);

        // Default view is Month
        if (monthViewButton != null) {
            monthViewButton.setDisable(true); // indicate active
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

    // -------------------- View Mode Handlers --------------------
    @FXML
    private void handleMonthView() {
        currentViewMode = ViewMode.MONTH;
        currentYearMonth = YearMonth.from(selectedDate);
        syncViewButtons();
        buildCalendar();
    }

    @FXML
    private void handleWeekView() {
        currentViewMode = ViewMode.WEEK;
        currentYearMonth = YearMonth.from(selectedDate);
        syncViewButtons();
        buildCalendar();
    }

    @FXML
    private void handleDayView() {
        currentViewMode = ViewMode.DAY;
        currentYearMonth = YearMonth.from(selectedDate);
        syncViewButtons();
        buildCalendar();
    }

    private void syncViewButtons() {
        if (monthViewButton != null) monthViewButton.setDisable(currentViewMode == ViewMode.MONTH);
        if (weekViewButton != null) weekViewButton.setDisable(currentViewMode == ViewMode.WEEK);
        if (dayViewButton != null) dayViewButton.setDisable(currentViewMode == ViewMode.DAY);
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
        switch (currentViewMode) {
            case MONTH -> {
                currentYearMonth = currentYearMonth.minusMonths(1);
                // Keep selectedDate within month
                selectedDate = currentYearMonth.atDay(Math.min(selectedDate.getDayOfMonth(), currentYearMonth.lengthOfMonth()));
            }
            case WEEK -> selectedDate = selectedDate.minusWeeks(1);
            case DAY -> selectedDate = selectedDate.minusDays(1);
        }
        currentYearMonth = YearMonth.from(selectedDate);
        buildCalendar();
        updateDayReminders();
        updateSelectedDateLabel();
    }

    @FXML
    private void handleNextMonth() {
        switch (currentViewMode) {
            case MONTH -> {
                currentYearMonth = currentYearMonth.plusMonths(1);
                selectedDate = currentYearMonth.atDay(Math.min(selectedDate.getDayOfMonth(), currentYearMonth.lengthOfMonth()));
            }
            case WEEK -> selectedDate = selectedDate.plusWeeks(1);
            case DAY -> selectedDate = selectedDate.plusDays(1);
        }
        currentYearMonth = YearMonth.from(selectedDate);
        buildCalendar();
        updateDayReminders();
        updateSelectedDateLabel();
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
        currentYearMonth = YearMonth.from(selectedDate);
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
        currentYearMonth = YearMonth.from(selectedDate);
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

        switch (currentViewMode) {
            case MONTH -> buildMonthView();
            case WEEK -> buildWeekView();
            case DAY -> buildDayView();
        }

        updateMonthLabel();
        highlightSelectedDay();
    }

    private void buildMonthView() {
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

        // Header row
        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

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

        updateDayReminders();
    }

    private void buildWeekView() {
        // 7 columns (Mon–Sun)
        for (int col = 0; col < 7; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            cc.setPercentWidth(100.0 / 7.0);
            calendarGrid.getColumnConstraints().add(cc);
        }

        // 2 rows: header + days row
        for (int row = 0; row < 2; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            rc.setPercentHeight(row == 0 ? 15.0 : 85.0);
            calendarGrid.getRowConstraints().add(rc);
        }

        // Header row
        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (int i = 0; i < days.length; i++) {
            Label label = new Label(days[i].name().substring(0, 3));
            label.getStyleClass().add("calendar-header");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setVgrow(label, Priority.ALWAYS);
            calendarGrid.add(label, i, 0);
        }

        // Week that contains selectedDate (starting Monday)
        int offset = (selectedDate.getDayOfWeek().getValue() + 6) % 7;
        LocalDate weekStart = selectedDate.minusDays(offset);

        for (int col = 0; col < 7; col++) {
            LocalDate date = weekStart.plusDays(col);
            VBox cell = createDayCell(date);
            calendarGrid.add(cell, col, 1);
        }

        updateDayReminders();
    }

    private void buildDayView() {
        // 1 column, 2 rows (header + day cell)
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        cc.setFillWidth(true);
        cc.setPercentWidth(100.0);
        calendarGrid.getColumnConstraints().add(cc);

        for (int row = 0; row < 2; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setFillHeight(true);
            rc.setPercentHeight(row == 0 ? 20.0 : 80.0);
            calendarGrid.getRowConstraints().add(rc);
        }

        // Header: Day name
        String headerText = selectedDate.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.getDefault());
        Label header = new Label(headerText);
        header.getStyleClass().add("calendar-header");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        GridPane.setHgrow(header, Priority.ALWAYS);
        GridPane.setVgrow(header, Priority.ALWAYS);
        calendarGrid.add(header, 0, 0);

        // One big day cell
        VBox cell = createDayCell(selectedDate);
        calendarGrid.add(cell, 0, 1);

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
                if (rowIdx == null) continue;

                for (var child : cell.getChildren()) {
                    if (child instanceof Label label) {
                        try {
                            int day = Integer.parseInt(label.getText());
                            LocalDate date;

                            // In WEEK view, date might not be in currentYearMonth
                            if (currentViewMode == ViewMode.MONTH) {
                                date = currentYearMonth.atDay(day);
                            } else {
                                // We stored the correct date when creating cells;
                                // here we approximate by checking against selectedDate:
                                // but since we only toggle selection visually,
                                // just compare day-of-month and month/year.
                                // To be safe, skip changing selection here if mismatch.
                                // Simpler: just compare day-of-month + month+year.
                                // However easiest: ignore and only highlight if same day-of-month
                                // AND same month/year:
                                date = selectedDate;
                            }

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

        DateTimeFormatter monthYearFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
        DateTimeFormatter shortDateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        switch (currentViewMode) {
            case MONTH -> monthYearLabel.setText(currentYearMonth.format(monthYearFmt));
            case WEEK -> {
                int offset = (selectedDate.getDayOfWeek().getValue() + 6) % 7;
                LocalDate weekStart = selectedDate.minusDays(offset);
                LocalDate weekEnd = weekStart.plusDays(6);
                monthYearLabel.setText("Week of " + weekStart.format(shortDateFmt)
                        + " – " + weekEnd.format(shortDateFmt));
            }
            case DAY -> monthYearLabel.setText(selectedDate.format(fullDateFmt));
        }
    }

    private void updateSelectedDateLabel() {
        if (selectedDateLabel != null && selectedDate != null) {
            selectedDateLabel.setText("Selected: " + selectedDate.toString());
        }
    }
}
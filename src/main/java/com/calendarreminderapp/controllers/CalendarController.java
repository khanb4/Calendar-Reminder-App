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
import javafx.geometry.Orientation;


import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class CalendarController {

    private enum ViewMode {YEAR, MONTH, WEEK, DAY}

    private ViewMode currentViewMode = ViewMode.MONTH;

    @FXML
    private Label monthYearLabel;
    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label selectedDateLabel;

    @FXML
    private ListView<Reminder> dayRemindersList;
    @FXML
    private ListView<Reminder> upcomingRemindersList;

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
    private Button editReminderButton;
    @FXML
    private Button deleteReminderButton;
    @FXML
    private Button backButton;

    @FXML
    private Button monthViewButton;
    @FXML
    private Button weekViewButton;
    @FXML
    private Button dayViewButton;
    @FXML
    private Button yearViewButton;

    private ReminderRepository reminderRepository;
    private String currentUser;

    private YearMonth currentYearMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    private final Pane[] daySlots = new Pane[24];


    private Reminder selectedReminder;

    // EDIT MODE switch
    private boolean isEditing = false;

    public void setCurrentUser(String user) {
        this.currentUser = user;
        buildCalendar();
        loadUpcomingReminders();
        updateDayReminders();
    }

    @FXML
    public void initialize() {

        try {
            reminderRepository = new ReminderRepository();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupTimeDropdowns();

        buildCalendar();
        updateSelectedDateLabel();
        updateDayReminders();
        loadUpcomingReminders();

        editReminderButton.setDisable(true);
        deleteReminderButton.setDisable(true);

        // Render reminders nicely
        dayRemindersList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reminder r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else {
                    String text = r.getTime() + " — " + r.getTitle();
                    if (r.getDescription() != null && !r.getDescription().isBlank()) {
                        text += "\n" + r.getDescription();
                    }
                    setText(text);
                }
            }
        });

        // Selection listener for reminder list
        dayRemindersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, r) -> {
            selectedReminder = r;

            boolean valid = (r != null);
            editReminderButton.setDisable(!valid);
            deleteReminderButton.setDisable(!valid);

            if (valid) {
                populateForm(r);
                isEditing = false; // user must click Edit button first
                addReminderButton.setText("Save Reminder");
                addReminderButton.setDisable(false);
            }
        });

        // Upcoming uses read-only style
        upcomingRemindersList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reminder r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else {
                    String text = r.getDate() + "  " + r.getTime() + " — " + r.getTitle();
                    if (r.getDescription() != null && !r.getDescription().isBlank()) {
                        text += "\n" + r.getDescription();
                    }
                    setText(text);
                }
            }
        });
    }

    /**
     * ✅ UPDATED:
     * - Fill items
     * - Set DEFAULT selected values so the ComboBoxes are not blank
     * - Keep formatting consistent with your Reminder time parsing
     */
    private void setupTimeDropdowns() {
        hourCombo.getItems().clear();
        minuteCombo.getItems().clear();
        ampmCombo.getItems().clear();

        // IMPORTANT: Your populateForm() selects hm[0] directly.
        // If your stored times are like "7:04 AM" (no leading 0),
        // then hours must be "1"."12" (not "01"."12").
        for (int i = 1; i <= 12; i++) {
            hourCombo.getItems().add(String.valueOf(i));
        }

        for (int i = 0; i < 60; i++) {
            minuteCombo.getItems().add(String.format("%02d", i));
        }

        ampmCombo.getItems().addAll("AM", "PM");

        // ✅ DEFAULTS (this is what makes Hr/Min show like AM/PM does)
        hourCombo.setValue("1");
        minuteCombo.setValue("00");
        ampmCombo.setValue("AM");
    }

    @FXML
    private void handlePrevMonth() {
        switch (currentViewMode) {
            case MONTH -> currentYearMonth = currentYearMonth.minusMonths(1);
            case WEEK -> selectedDate = selectedDate.minusWeeks(1);
            case DAY -> selectedDate = selectedDate.minusDays(1);
            case YEAR -> currentYearMonth = currentYearMonth.minusYears(1);
        }
        refresh();
    }

    @FXML
    private void handleNextMonth() {
        switch (currentViewMode) {
            case MONTH -> currentYearMonth = currentYearMonth.plusMonths(1);
            case WEEK -> selectedDate = selectedDate.plusWeeks(1);
            case DAY -> selectedDate = selectedDate.plusDays(1);
            case YEAR -> currentYearMonth = currentYearMonth.plusYears(1);
        }
        refresh();
    }

    @FXML
    private void handleMonthView() {
        currentViewMode = ViewMode.MONTH;
        monthViewButton.setDisable(true);
        weekViewButton.setDisable(false);
        dayViewButton.setDisable(false);
        yearViewButton.setDisable(false);
        refresh();
    }

    @FXML
    private void handleWeekView() {
        currentViewMode = ViewMode.WEEK;
        monthViewButton.setDisable(false);
        weekViewButton.setDisable(true);
        dayViewButton.setDisable(false);
        yearViewButton.setDisable(false);
        refresh();
    }

    @FXML
    private void handleDayView() {
        currentViewMode = ViewMode.DAY;
        monthViewButton.setDisable(false);
        weekViewButton.setDisable(false);
        dayViewButton.setDisable(true);
        yearViewButton.setDisable(false);
        refresh();
    }

    @FXML
    private void handleYearView() {
        currentViewMode = ViewMode.YEAR;
        yearViewButton.setDisable(true);
        monthViewButton.setDisable(false);
        weekViewButton.setDisable(false);
        dayViewButton.setDisable(false);
        refresh();
    }

    // ============================================================
    //   ADD OR EDIT Reminder (Unified Handler)
    // ============================================================
    @FXML
    private void handleAddReminder() {

        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        String ampm = ampmCombo.getValue();

        if (title.isEmpty() || hour == null || minute == null || ampm == null) return;

        String time = hour + ":" + minute + " " + ampm;

        try {
            if (isEditing && selectedReminder != null) {

                // ─────────────── UPDATE MODE ───────────────
                reminderRepository.updateReminder(
                        selectedReminder,
                        selectedDate,
                        title,
                        desc,
                        time
                );

            } else {

                // ─────────────── ADD MODE ───────────────
                reminderRepository.addReminder(
                        currentUser,
                        selectedDate,
                        title,
                        desc,
                        time
                );
            }

            // Reset form
            clearForm();
            isEditing = false;
            selectedReminder = null;

            addReminderButton.setText("Save Reminder");
            editReminderButton.setDisable(true);
            deleteReminderButton.setDisable(true);

            refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    //   ENTER EDIT MODE
    // ============================================================
    @FXML
    private void handleEditReminder() {
        if (selectedReminder == null) return;

        isEditing = true;

        addReminderButton.setText("Save Changes");
        addReminderButton.setDisable(false);
    }

    @FXML
    private void handleDeleteReminder() {
        if (selectedReminder == null) return;

        try {
            reminderRepository.deleteReminder(selectedReminder);

            clearForm();
            selectedReminder = null;
            isEditing = false;

            addReminderButton.setText("Save Reminder");
            editReminderButton.setDisable(true);
            deleteReminderButton.setDisable(true);

            refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/calendarreminderapp/login.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    //   SUPPORT METHODS
    // ============================================================
    private void populateForm(Reminder r) {
        titleField.setText(r.getTitle());
        descriptionArea.setText(r.getDescription() == null ? "" : r.getDescription());

        try {
            String[] parts = r.getTime().split(" ");
            String[] hm = parts[0].split(":");

            hourCombo.getSelectionModel().select(hm[0]);
            minuteCombo.getSelectionModel().select(hm[1]);
            ampmCombo.getSelectionModel().select(parts[1]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OPTIONAL BEHAVIOR:
     * If you want Hr/Min to go back to defaults after saving, keep as-is below.
     * If you want them blank, replace setValue(...) with clearSelection().
     */
    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();

        // ✅ default back to a valid time so the fields never look empty
        hourCombo.setValue("1");
        minuteCombo.setValue("00");
        ampmCombo.setValue("AM");
    }

    private void updateDayReminders() {
        try {
            List<Reminder> list =
                    reminderRepository.getRemindersForDate(currentUser, selectedDate);
            list.sort(Comparator.comparing(Reminder::getTime));
            dayRemindersList.getItems().setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUpcomingReminders() {
        try {
            List<Reminder> list =
                    reminderRepository.getRemindersForMonth(currentUser, currentYearMonth);

            list.sort(
                    Comparator.comparing((Reminder r) -> LocalDate.parse(r.getDate()))
                            .thenComparing(Reminder::getTime)
            );

            upcomingRemindersList.getItems().setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void buildCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        switch (currentViewMode) {
            case YEAR -> buildYearView();
            case MONTH -> buildMonthView();
            case WEEK -> buildWeekView();
            case DAY -> buildDayView();
        }

        updateMonthLabel();
        highlightSelectedDay();
    }

    private VBox buildMonthCard(YearMonth ym) {
        VBox box = new VBox();
        box.getStyleClass().add("year-month-card");
        box.setSpacing(6);
        box.setPadding(new Insets(10));

        Label title = new Label(ym.getMonth().name());
        title.getStyleClass().add("year-month-title");
        box.getChildren().add(title);

        LocalDate first = ym.atDay(1);
        int length = ym.lengthOfMonth();

        FlowPane grid = new FlowPane();
        grid.setHgap(4);
        grid.setVgap(4);

        for (int d = 1; d <= length; d++) {
            LocalDate date = ym.atDay(d);
            Label dayLabel = new Label(String.valueOf(d));
            dayLabel.getStyleClass().add("year-day-number");

            dayLabel.setOnMouseClicked(e -> {
                selectedDate = date;
                currentYearMonth = ym;
                currentViewMode = ViewMode.MONTH;
                refresh();
            });

            grid.getChildren().add(dayLabel);
        }

        box.getChildren().add(grid);
        return box;
    }

    private void buildYearView() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // 4 rows × 3 columns = 12 months
        for (int c = 0; c < 3; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 3);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 4; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 4);
            rc.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rc);
        }

        for (int i = 0; i < 12; i++) {
            YearMonth ym = YearMonth.of(currentYearMonth.getYear(), i + 1);
            VBox monthCard = buildMonthCard(ym);
            int row = i / 3;
            int col = i % 3;
            calendarGrid.add(monthCard, col, row);
        }

        monthYearLabel.setText(String.valueOf(currentYearMonth.getYear()));
    }

    private void buildMonthView() {
        for (int c = 0; c < 7; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 7; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 7);
            rc.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(rc);
        }

        DayOfWeek[] days = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (int i = 0; i < 7; i++) {
            Label label = new Label(days[i].name().substring(0, 3));
            label.setAlignment(Pos.CENTER);
            label.getStyleClass().add("calendar-header");
            calendarGrid.add(label, i, 0);
        }

        LocalDate first = currentYearMonth.atDay(1);
        int firstCol = (first.getDayOfWeek().getValue() + 6) % 7;
        int length = currentYearMonth.lengthOfMonth();

        int day = 1;
        int row = 1;

        while (day <= length) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < firstCol) continue;
                if (day > length) break;

                LocalDate date = currentYearMonth.atDay(day);
                calendarGrid.add(createDayCell(date), col, row);

                day++;
            }
            row++;
        }
    }

    private void buildWeekView() {

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        LocalDate weekStart = selectedDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }

        RowConstraints rc = new RowConstraints();
        rc.setPercentHeight(100);
        rc.setVgrow(Priority.ALWAYS);
        calendarGrid.getRowConstraints().add(rc);

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            VBox cell = buildWeekDayCell(date);
            calendarGrid.add(cell, i, 0);
        }

        monthYearLabel.setText("Week of " +
                weekStart.format(DateTimeFormatter.ofPattern("MMM d")) +
                " - " +
                weekEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
    }

    private VBox buildWeekDayCell(LocalDate date) {
        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell");
        cell.setSpacing(8);
        cell.setPadding(new Insets(10));

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        try {
            List<Reminder> reminders = reminderRepository.getRemindersForDate(currentUser, date);
            for (Reminder r : reminders) {
                Label lbl = new Label(r.getTime() + " — " + r.getTitle());
                lbl.setWrapText(true);
                lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #5F6368;");
                cell.getChildren().add(lbl);
            }
        } catch (Exception ignored) {
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            refresh();
        });

        if (date.equals(selectedDate)) {
            cell.getStyleClass().add("calendar-day-selected");
        }

        return cell;
    }

    private void buildDayView() {

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // 2 columns: time labels + event grid
        ColumnConstraints timeCol = new ColumnConstraints();
        timeCol.setMinWidth(90);
        timeCol.setPrefWidth(90);
        timeCol.setHgrow(Priority.NEVER);

        ColumnConstraints gridCol = new ColumnConstraints();
        gridCol.setHgrow(Priority.ALWAYS);
        gridCol.setFillWidth(true);

        calendarGrid.getColumnConstraints().addAll(timeCol, gridCol);

        for (int i = 0; i < 24; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(50);
            rc.setPrefHeight(50);
            rc.setVgrow(Priority.NEVER);
            calendarGrid.getRowConstraints().add(rc);

            // Time label (12-hr)
            Label hourLabel = new Label(formatHour12(i));
            hourLabel.getStyleClass().add("day-time-label");
            hourLabel.setMinWidth(90);
            hourLabel.setPadding(new Insets(6, 8, 0, 8));

            // ✅ Slot pane for events (SIDE-BY-SIDE)
            HBox slot = new HBox(10);
            slot.getStyleClass().add("day-slot");
            slot.setAlignment(Pos.CENTER_LEFT);
            slot.setMinHeight(50);
            slot.setMaxWidth(Double.MAX_VALUE);

            daySlots[i] = slot;

            calendarGrid.add(hourLabel, 0, i);
            calendarGrid.add(slot, 1, i);
        }

        try {
            List<Reminder> list = reminderRepository.getRemindersForDate(currentUser, selectedDate);

            for (Reminder r : list) {
                int hour = parseHour(r.getTime());

                VBox eventBox = new VBox();
                eventBox.getStyleClass().add("day-event");

                Label lbl = new Label(r.getTime() + " — " + r.getTitle());
                lbl.setWrapText(true);
                lbl.getStyleClass().add("day-event-text");
                eventBox.getChildren().add(lbl);

                // Put the event inside the hour slot
                HBox slot = (HBox) daySlots[hour];

                // ✅ add vertical divider if not first item
                if (!slot.getChildren().isEmpty()) {
                    Separator sep = new Separator(Orientation.VERTICAL);
                    sep.getStyleClass().add("day-event-vsep");
                    slot.getChildren().add(sep);
                }

                slot.getChildren().add(eventBox);
                HBox.setMargin(eventBox, new Insets(6, 6, 6, 6));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        monthYearLabel.setText(
                selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy"))
        );
    }


    private String formatHour12(int hour24) {
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;

        String ampm = (hour24 < 12) ? "AM" : "PM";
        return hour12 + ":00 " + ampm;
    }


    private int parseHour(String time) {
        try {
            String[] parts = time.split(" ");
            String[] hm = parts[0].split(":");

            int hour = Integer.parseInt(hm[0]);
            boolean pm = parts[1].equalsIgnoreCase("PM");

            if (pm && hour != 12) hour += 12;
            if (!pm && hour == 12) hour = 0;

            return hour;
        } catch (Exception e) {
            return 0;
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox();
        cell.getStyleClass().add("day-cell");
        cell.setPadding(new Insets(6));
        cell.setSpacing(4);

        Label num = new Label(String.valueOf(date.getDayOfMonth()));
        num.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(num);

        try {
            int count = reminderRepository.getRemindersForDate(currentUser, date).size();
            if (count > 0) {
                Label dot = new Label("• " + count + " reminder(s)");
                dot.getStyleClass().add("calendar-reminder-icon");
                cell.getChildren().add(dot);
            }
        } catch (Exception ignored) {
        }

        cell.setOnMouseClicked(e -> handleDayClicked(date));
        return cell;
    }

    private void handleDayClicked(LocalDate date) {
        selectedDate = date;
        isEditing = false;
        selectedReminder = null;

        addReminderButton.setText("Save Reminder");
        addReminderButton.setDisable(false);

        clearForm();
        refresh();
    }

    private void highlightSelectedDay() {
        for (var node : calendarGrid.getChildren()) {
            node.getStyleClass().remove("calendar-day-selected");

            if (node instanceof VBox cell) {
                for (var child : cell.getChildren()) {
                    if (child instanceof Label lbl) {
                        try {
                            int day = Integer.parseInt(lbl.getText());
                            LocalDate date = currentYearMonth.atDay(day);
                            if (date.equals(selectedDate)) {
                                cell.getStyleClass().add("calendar-day-selected");
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private void updateMonthLabel() {
        switch (currentViewMode) {
            case YEAR -> monthYearLabel.setText(String.valueOf(currentYearMonth.getYear()));
            case MONTH -> monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            case WEEK -> monthYearLabel.setText("Week View");
            case DAY -> monthYearLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")));
        }
    }

    private void updateSelectedDateLabel() {
        selectedDateLabel.setText("Selected: " + selectedDate);
    }

    private void refresh() {
        buildCalendar();
        updateDayReminders();
        loadUpcomingReminders();
        updateSelectedDateLabel();
    }

    @FXML
    private void handleToday() {
        selectedDate = LocalDate.now();
        currentYearMonth = YearMonth.now();

        // Ensure view stays consistent
        if (currentViewMode == ViewMode.WEEK || currentViewMode == ViewMode.DAY) {
            // selectedDate already set correctly
        } else {
            currentViewMode = ViewMode.MONTH;
        }

        refresh();
    }

    @FXML
    private void openHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/calendarreminderapp/help-view.fxml")
            );
            Parent root = loader.load();

            Stage ownerStage = (Stage) calendarGrid.getScene().getWindow();

            Stage helpStage = new Stage();
            helpStage.initOwner(ownerStage);
            helpStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            helpStage.setTitle("Help & User Guide");
            helpStage.setScene(new Scene(root, 900, 650));
            helpStage.setResizable(true);

            helpStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

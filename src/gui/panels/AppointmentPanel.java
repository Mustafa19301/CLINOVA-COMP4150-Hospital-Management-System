package gui.panels;

import dao.AppointmentDAO;
import dao.PatientDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentPanel extends JPanel {

    private static final Color DARK = new Color(16, 45, 56);
    private static final Color TEAL = new Color(23, 107, 104);
    private static final Color BACKGROUND = new Color(243, 247, 246);
    private static final Color TEXT = new Color(35, 58, 62);
    private static final Color MUTED = new Color(112, 132, 134);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER = new Color(225, 233, 231);

    private static final Color GREEN = new Color(35, 125, 91);
    private static final Color BLUE = new Color(65, 105, 160);
    private static final Color RED = new Color(177, 76, 76);
    private static final Color LIGHT_TEAL = new Color(232, 243, 240);

    private static final Color WEEKDAY_HEADER = new Color(28, 68, 75);
    private static final Color WEEKEND_BACKGROUND = new Color(248, 250, 249);
    private static final Color OUTSIDE_MONTH = new Color(244, 247, 246);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a");

    private final int currentStaffId;
    private final String currentStaffName;
    private final String currentRole;

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;

    private List<Map<String, Object>> appointments =
            new ArrayList<>();

    private JPanel agendaPanel;
    private JPanel summaryPanel;

    private JLabel calendarMonthLabel;

    private LocalDate calendarMonth =
            LocalDate.now().withDayOfMonth(1);

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel appointmentCountLabel;

    public AppointmentPanel(
            int staffId,
            String staffName,
            String role) {

        this.currentStaffId = staffId;
        this.currentStaffName = staffName;
        this.currentRole = normalizeRole(role);

        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        initializeUI();
        loadAppointments();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 22));
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        mainPanel.add(createHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 22));
        centerPanel.setBackground(BACKGROUND);

        summaryPanel = createSummaryPanel();

        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(createAgendaSection(), BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(5, 2, 5, 2));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(
                titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appointment Scheduler");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Coordinate patient visits & manage your schedule");

        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton bookButton =
                createPrimaryButton("+  Book Appointment");

        bookButton.addActionListener(e -> showBookingDialog());

        JButton refreshButton =
                createSecondaryButton("↻  Refresh");

        refreshButton.addActionListener(e -> loadAppointments());

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 9, 0));

        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(bookButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel createSummaryPanel() {

        JPanel panel = new JPanel(
                new GridLayout(1, 3, 0, 0));

        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(13, 12, 13, 12)));

        panel.add(createSummaryCard(
                "SCHEDULED",
                "0",
                TEAL,
                new Color(232, 243, 240)));

        panel.add(createSummaryCard(
                "COMPLETED",
                "0",
                BLUE,
                new Color(235, 240, 248)));

        panel.add(createSummaryCard(
                "CANCELLED",
                "0",
                RED,
                new Color(249, 237, 237)));

        return panel;
    }

    private JPanel createSummaryCard(
            String label,
            String value,
            Color accent,
            Color lightBackground) {

        JPanel segment = new JPanel(
                new BorderLayout(12, 0));

        segment.setOpaque(false);
        segment.setBorder(new EmptyBorder(4, 17, 4, 17));

        JPanel marker = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(lightBackground);

                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(accent);

                g2.fillRoundRect(
                        8, 9, 4,
                        Math.max(8, getHeight() - 18),
                        4, 4);

                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 38);
            }
        };

        marker.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(
                textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 9));
        title.setForeground(MUTED);

        JLabel count = new JLabel(value);
        count.setFont(new Font("SansSerif", Font.BOLD, 23));
        count.setForeground(DARK);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(count);

        segment.add(marker, BorderLayout.WEST);
        segment.add(textPanel, BorderLayout.CENTER);

        segment.putClientProperty("countLabel", count);

        return segment;
    }

    private JPanel createAgendaSection() {

        JPanel section = new JPanel(
                new BorderLayout(0, 12));

        section.setBackground(BACKGROUND);

        JPanel topArea = new JPanel(
                new BorderLayout(12, 12));

        topArea.setBackground(BACKGROUND);

        JPanel toolbar = new JPanel(
                new BorderLayout(15, 0));

        toolbar.setBackground(BACKGROUND);

        JPanel sectionTitle = new JPanel();

        sectionTitle.setOpaque(false);
        sectionTitle.setLayout(new BoxLayout(
                sectionTitle, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appointment Calendar");
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "View and manage scheduled visits across the month");

        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        sectionTitle.add(title);
        sectionTitle.add(Box.createVerticalStrut(4));
        sectionTitle.add(subtitle);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        filters.setOpaque(false);

        searchField = new JTextField(13);
        searchField.setFont(
                new Font("SansSerif", Font.PLAIN, 12));

        searchField.setPreferredSize(
                new Dimension(165, 35));

        searchField.setToolTipText("Search by patient name");

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(0, 10, 0, 10)));

        searchField.addActionListener(
                e -> loadAppointments());

        JButton searchButton =
                createSecondaryButton("Search");

        searchButton.addActionListener(
                e -> loadAppointments());

        statusFilter = new JComboBox<>(new String[]{
                "All Statuses",
                "Scheduled",
                "Completed",
                "Cancelled"
        });

        statusFilter.setFont(
                new Font("SansSerif", Font.PLAIN, 11));

        statusFilter.setPreferredSize(
                new Dimension(132, 35));

        statusFilter.setBackground(WHITE);

        statusFilter.addActionListener(
                e -> renderAgenda());

        filters.add(searchField);
        filters.add(searchButton);
        filters.add(statusFilter);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(filters, BorderLayout.EAST);

        JPanel calendarToolbar = new JPanel(
                new BorderLayout());

        calendarToolbar.setOpaque(false);
        calendarToolbar.setBorder(
                new EmptyBorder(2, 0, 2, 0));

        JPanel monthControls = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 7, 0));

        monthControls.setOpaque(false);

        JButton previousMonth =
                createSecondaryButton("‹");

        previousMonth.setToolTipText("Previous month");

        previousMonth.addActionListener(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            renderAgenda();
        });

        JButton nextMonth =
                createSecondaryButton("›");

        nextMonth.setToolTipText("Next month");

        nextMonth.addActionListener(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            renderAgenda();
        });

        calendarMonthLabel = new JLabel();

        calendarMonthLabel.setFont(
                new Font("SansSerif", Font.BOLD, 17));

        calendarMonthLabel.setForeground(DARK);

        calendarMonthLabel.setBorder(
                new EmptyBorder(0, 8, 0, 8));

        monthControls.add(previousMonth);
        monthControls.add(nextMonth);
        monthControls.add(calendarMonthLabel);

        JLabel legend = new JLabel(
                "<html>"
                        + "<span style='color:#237D5B'>●</span> Scheduled&nbsp;&nbsp;"
                        + "<span style='color:#4169A0'>●</span> Completed&nbsp;&nbsp;"
                        + "<span style='color:#B14C4C'>●</span> Cancelled"
                        + "</html>");

        legend.setFont(
                new Font("SansSerif", Font.BOLD, 9));

        legend.setForeground(MUTED);

        calendarToolbar.add(
                monthControls, BorderLayout.WEST);

        calendarToolbar.add(
                legend, BorderLayout.EAST);

        topArea.add(toolbar, BorderLayout.NORTH);
        topArea.add(calendarToolbar, BorderLayout.SOUTH);

        agendaPanel = new JPanel(
                new GridLayout(7, 7, 7, 7));

        agendaPanel.setBackground(BACKGROUND);

        agendaPanel.setBorder(
                new EmptyBorder(2, 1, 10, 1));

        JScrollPane scrollPane =
                new JScrollPane(agendaPanel);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder());

        scrollPane.getViewport().setBackground(BACKGROUND);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(20);

        scrollPane.getVerticalScrollBar()
                .setPreferredSize(new Dimension(8, 0));

        scrollPane.getHorizontalScrollBar()
                .setUnitIncrement(16);

        appointmentCountLabel = new JLabel(
                "0 appointments");

        appointmentCountLabel.setFont(
                new Font("SansSerif", Font.BOLD, 10));

        appointmentCountLabel.setForeground(MUTED);

        JPanel footer = new JPanel(
                new BorderLayout());

        footer.setOpaque(false);

        footer.setBorder(
                new EmptyBorder(2, 2, 0, 2));

        footer.add(
                appointmentCountLabel, BorderLayout.WEST);

        section.add(topArea, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);
        section.add(footer, BorderLayout.SOUTH);

        return section;
    }

    private void loadAppointments() {

        try {

            String search = searchField == null
                    ? ""
                    : searchField.getText().trim();

            if (search.isEmpty()) {

                appointments =
                        appointmentDAO.getAppointmentsForStaff(
                                currentStaffId,
                                currentRole
                        );

            } else {

                appointments =
                        appointmentDAO.searchAppointmentsForStaff(
                                currentStaffId,
                                currentRole,
                                search
                        );
            }

            updateSummary();
            renderAgenda();

        } catch (SQLException exception) {

            showError(
                    "Unable to load appointments:\n"
                            + exception.getMessage()
            );
        }
    }

    private void updateSummary() {

        int scheduled = 0;
        int completed = 0;
        int cancelled = 0;

        for (Map<String, Object> appointment : appointments) {

            String status =
                    getString(appointment, "status");

            if ("Scheduled".equalsIgnoreCase(status)) {
                scheduled++;
            } else if ("Completed".equalsIgnoreCase(status)) {
                completed++;
            } else if ("Cancelled".equalsIgnoreCase(status)) {
                cancelled++;
            }
        }

        updateSummaryValue(0, String.valueOf(scheduled));
        updateSummaryValue(1, String.valueOf(completed));
        updateSummaryValue(2, String.valueOf(cancelled));
    }

    private void updateSummaryValue(
            int index,
            String value) {

        if (summaryPanel == null) {
            return;
        }

        JPanel card =
                (JPanel) summaryPanel.getComponent(index);

        JLabel label =
                (JLabel) card.getClientProperty("countLabel");

        if (label != null) {
            label.setText(value);
        }
    }

    private void renderAgenda() {

        if (agendaPanel == null) {
            return;
        }

        agendaPanel.removeAll();


        agendaPanel.setLayout(
                new GridLayout(7, 7, 7, 7));

        String selectedStatus = statusFilter == null
                ? "All Statuses"
                : (String) statusFilter.getSelectedItem();

        Map<LocalDate, List<Map<String, Object>>> grouped =
                new LinkedHashMap<>();

        for (Map<String, Object> appointment : appointments) {

            String status =
                    getString(appointment, "status");

            if (!"All Statuses".equals(selectedStatus)
                    && !selectedStatus.equalsIgnoreCase(status)) {

                continue;
            }

            LocalDate date = getDate(
                    appointment.get("appointment_date"));

            if (date != null) {

                grouped.computeIfAbsent(
                        date,
                        key -> new ArrayList<>()
                ).add(appointment);
            }
        }

        calendarMonthLabel.setText(
                calendarMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy")));

        String[] weekdays = {
                "SUNDAY",
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY"
        };

        for (String weekday : weekdays) {

            JPanel header = new JPanel(
                    new BorderLayout());

            header.setBackground(WEEKDAY_HEADER);

            header.setBorder(
                    BorderFactory.createCompoundBorder(
                            new LineBorder(
                                    WEEKDAY_HEADER.darker(), 1, true),
                            new EmptyBorder(10, 3, 10, 3)));

            JLabel dayLabel = new JLabel(
                    weekday, SwingConstants.CENTER);

            dayLabel.setFont(
                    new Font("SansSerif", Font.BOLD, 10));

            dayLabel.setForeground(Color.WHITE);

            header.add(dayLabel, BorderLayout.CENTER);

            agendaPanel.add(header);
        }


        LocalDate firstOfMonth =
                calendarMonth.withDayOfMonth(1);

        int daysBeforeFirst =
                firstOfMonth.getDayOfWeek().getValue() % 7;

        LocalDate gridStart =
                firstOfMonth.minusDays(daysBeforeFirst);

        int monthAppointmentCount = 0;

        for (int i = 0; i < 42; i++) {

            LocalDate date = gridStart.plusDays(i);

            List<Map<String, Object>> dayAppointments =
                    grouped.getOrDefault(
                            date,
                            new ArrayList<>());

            if (date.getMonth() == calendarMonth.getMonth()
                    && date.getYear() == calendarMonth.getYear()) {

                monthAppointmentCount +=
                        dayAppointments.size();
            }

            agendaPanel.add(
                    createCalendarDayCell(
                            date,
                            dayAppointments));
        }

        appointmentCountLabel.setText(
                monthAppointmentCount
                        + (monthAppointmentCount == 1
                        ? " appointment in "
                        : " appointments in ")
                        + calendarMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy")));

        agendaPanel.revalidate();
        agendaPanel.repaint();
    }

    private JPanel createCalendarDayCell(
            LocalDate date,
            List<Map<String, Object>> dayAppointments) {

        boolean currentMonth =
                date.getMonth() == calendarMonth.getMonth()
                        && date.getYear() == calendarMonth.getYear();

        boolean today =
                date.equals(LocalDate.now());

        boolean weekend =
                date.getDayOfWeek().getValue() == 6
                        || date.getDayOfWeek().getValue() == 7;

        Color cellBackground;

        if (!currentMonth) {
            cellBackground = OUTSIDE_MONTH;
        } else if (weekend) {
            cellBackground = WEEKEND_BACKGROUND;
        } else {
            cellBackground = WHITE;
        }

        JPanel cell = new JPanel(
                new BorderLayout(0, 6));

        cell.setBackground(cellBackground);

        cell.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(
                                today ? TEAL : BORDER,
                                today ? 2 : 1,
                                true),
                        new EmptyBorder(7, 7, 6, 7)));

        cell.setPreferredSize(
                new Dimension(125, 132));

        cell.setMinimumSize(
                new Dimension(95, 115));

        JPanel dateHeader = new JPanel(
                new BorderLayout());

        dateHeader.setOpaque(false);

        JLabel dayNumber = new JLabel(
                String.valueOf(date.getDayOfMonth()));

        dayNumber.setFont(
                new Font("SansSerif", Font.BOLD, 13));

        dayNumber.setForeground(
                currentMonth ? DARK : new Color(169, 184, 181));

        dateHeader.add(
                dayNumber, BorderLayout.WEST);

        if (today) {

            JLabel todayTag = new JLabel("TODAY");

            todayTag.setFont(
                    new Font("SansSerif", Font.BOLD, 8));

            todayTag.setForeground(WHITE);
            todayTag.setOpaque(true);
            todayTag.setBackground(TEAL);

            todayTag.setBorder(
                    new EmptyBorder(3, 6, 3, 6));

            dateHeader.add(
                    todayTag, BorderLayout.EAST);
        }

        JPanel entries = new JPanel();

        entries.setOpaque(false);

        entries.setLayout(new BoxLayout(
                entries, BoxLayout.Y_AXIS));

        for (Map<String, Object> appointment : dayAppointments) {

            entries.add(
                    createCalendarAppointment(appointment));

            entries.add(
                    Box.createVerticalStrut(5));
        }

        if (dayAppointments.isEmpty()) {

            JLabel empty = new JLabel("No appointments");

            empty.setFont(
                    new Font("SansSerif", Font.PLAIN, 9));

            empty.setForeground(
                    new Color(178, 191, 188));

            empty.setAlignmentX(
                    Component.LEFT_ALIGNMENT);

            entries.add(Box.createVerticalStrut(5));
            entries.add(empty);
        }

        JScrollPane entriesScroll =
                new JScrollPane(entries);

        entriesScroll.setBorder(
                BorderFactory.createEmptyBorder());

        entriesScroll.setOpaque(false);

        entriesScroll.getViewport().setOpaque(false);

        entriesScroll.getVerticalScrollBar()
                .setUnitIncrement(12);

        entriesScroll.getVerticalScrollBar()
                .setPreferredSize(
                        new Dimension(5, 0));

        entriesScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        cell.add(dateHeader, BorderLayout.NORTH);
        cell.add(entriesScroll, BorderLayout.CENTER);

        return cell;
    }

    private JPanel createCalendarAppointment(Map<String, Object> appointment) {

        String status = getString(appointment, "status");
        String patient = getString(appointment, "patient_name");
        String reason = getString(appointment, "reason");

        Color statusAccent;
        Color statusBackground;

        if ("Completed".equalsIgnoreCase(status)) {
            statusAccent = BLUE;
            statusBackground = new Color(241, 245, 251);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            statusAccent = RED;
            statusBackground = new Color(251, 243, 243);
        } else {
            statusAccent = GREEN;
            statusBackground = new Color(242, 248, 246);
        }

        JPanel item = new JPanel(new BorderLayout(4, 2));
        item.setBackground(statusBackground);
        item.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(statusAccent, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));

        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        LocalTime time = getTime(appointment.get("appointment_time"));

        String timeText = time == null
                ? "--:--"
                : time.format(DateTimeFormatter.ofPattern("h:mm a"));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new BoxLayout(
                detailsPanel, BoxLayout.Y_AXIS
        ));

        JLabel timeLabel = new JLabel(timeText);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
        timeLabel.setForeground(statusAccent);

        JLabel patientLabel = new JLabel(patient);
        patientLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
        patientLabel.setForeground(DARK);

        String reasonPreview;

        if (reason == null || reason.trim().isEmpty()) {
            reasonPreview = "No reason provided";
        } else if (reason.length() > 19) {
            reasonPreview = reason.substring(0, 17) + "...";
        } else {
            reasonPreview = reason;
        }

        JLabel reasonLabel = new JLabel(reasonPreview);
        reasonLabel.setFont(new Font("SansSerif", Font.PLAIN, 8));
        reasonLabel.setForeground(MUTED);

        JLabel statusLabel = new JLabel(status.toUpperCase());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 7));
        statusLabel.setForeground(statusAccent);

        detailsPanel.add(timeLabel);
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(patientLabel);
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(reasonLabel);
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(statusLabel);

        JButton moreButton = new JButton("⋮");
        moreButton.setFont(new Font("SansSerif", Font.BOLD, 17));
        moreButton.setForeground(DARK);
        moreButton.setBackground(WHITE);
        moreButton.setOpaque(true);
        moreButton.setContentAreaFilled(true);
        moreButton.setFocusPainted(false);
        moreButton.setBorder(new LineBorder(BORDER, 1, true));
        moreButton.setPreferredSize(new Dimension(25, 25));
        moreButton.setMinimumSize(new Dimension(25, 25));
        moreButton.setMaximumSize(new Dimension(25, 25));
        moreButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        moreButton.setToolTipText("Appointment actions");

        int appointmentId = getInt(appointment, "appointment_id");

        JPopupMenu menu = new JPopupMenu();

        if ("Scheduled".equalsIgnoreCase(status)) {

            JMenuItem edit = new JMenuItem("Edit appointment");
            edit.addActionListener(e -> showEditDialog(appointment));

            JMenuItem complete = new JMenuItem("Mark as completed");
            complete.addActionListener(
                    e -> changeStatus(appointmentId, "Completed")
            );

            JMenuItem cancel = new JMenuItem("Cancel appointment");
            cancel.addActionListener(
                    e -> cancelAppointment(appointmentId)
            );

            menu.add(edit);
            menu.add(complete);
            menu.addSeparator();
            menu.add(cancel);

        } else {

            JMenuItem unavailable = new JMenuItem("No actions available");
            unavailable.setEnabled(false);
            menu.add(unavailable);
        }

        moreButton.addActionListener(e ->
                menu.show(moreButton, 0, moreButton.getHeight())
        );

        JPanel actionPanel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 0, 0
        ));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(29, 25));
        actionPanel.add(moreButton);

        JPanel top = new JPanel(new BorderLayout(3, 0));
        top.setOpaque(false);
        top.add(detailsPanel, BorderLayout.CENTER);
        top.add(actionPanel, BorderLayout.EAST);

        item.add(top, BorderLayout.CENTER);

        MouseAdapter detailsClick = new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showAppointmentDetails(appointment);
            }
        };

        item.addMouseListener(detailsClick);
        detailsPanel.addMouseListener(detailsClick);
        timeLabel.addMouseListener(detailsClick);
        patientLabel.addMouseListener(detailsClick);
        reasonLabel.addMouseListener(detailsClick);
        statusLabel.addMouseListener(detailsClick);

        item.setToolTipText("Click to view appointment details");

        return item;
    }

    private void showAppointmentDetails(
            Map<String, Object> appointment) {

        String patient = getString(appointment, "patient_name");
        String doctor = getString(appointment, "doctor_name");
        String nurse = getString(appointment, "nurse_name");
        String reason = getString(appointment, "reason");
        String status = getString(appointment, "status");

        LocalDate date = getDate(
                appointment.get("appointment_date")
        );

        LocalTime time = getTime(
                appointment.get("appointment_time")
        );

        String dateText = date == null
                ? "Not available"
                : date.format(DateTimeFormatter.ofPattern(
                "EEEE, MMMM d, yyyy"
        ));

        String timeText = time == null
                ? "Not available"
                : time.format(DateTimeFormatter.ofPattern("h:mm a"));

        JPanel detailsPanel = new JPanel(new BorderLayout(0, 14));
        detailsPanel.setBackground(WHITE);
        detailsPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel heading = new JLabel("Appointment Details");
        heading.setFont(new Font("SansSerif", Font.BOLD, 19));
        heading.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Review the information associated with this appointment."
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(
                headingPanel, BoxLayout.Y_AXIS
        ));

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(subtitle);

        JPanel information = new JPanel(new GridBagLayout());
        information.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(7, 0, 7, 0);

        addDetailRow(information, gbc, "Patient", patient);
        addDetailRow(information, gbc, "Doctor", doctor);
        addDetailRow(information, gbc, "Nurse", nurse);
        addDetailRow(information, gbc, "Date", dateText);
        addDetailRow(information, gbc, "Time", timeText);
        addDetailRow(
                information,
                gbc,
                "Status",
                status
        );

        JLabel reasonHeading = new JLabel("Appointment Reason");
        reasonHeading.setFont(new Font("SansSerif", Font.BOLD, 11));
        reasonHeading.setForeground(DARK);

        JTextArea reasonArea = new JTextArea(
                reason.isEmpty() ? "No reason provided." : reason
        );

        reasonArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reasonArea.setForeground(TEXT);
        reasonArea.setBackground(new Color(246, 249, 248));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setEditable(false);
        reasonArea.setFocusable(false);
        reasonArea.setBorder(new EmptyBorder(10, 11, 10, 11));

        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(new LineBorder(BORDER, 1, true));
        reasonScroll.setPreferredSize(new Dimension(390, 85));
        reasonScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel reasonPanel = new JPanel(new BorderLayout(0, 7));
        reasonPanel.setOpaque(false);
        reasonPanel.add(reasonHeading, BorderLayout.NORTH);
        reasonPanel.add(reasonScroll, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 13));
        content.setOpaque(false);
        content.add(headingPanel, BorderLayout.NORTH);
        content.add(information, BorderLayout.CENTER);
        content.add(reasonPanel, BorderLayout.SOUTH);

        detailsPanel.add(content, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                this,
                detailsPanel,
                "CLINOVA | Appointment",
                JOptionPane.PLAIN_MESSAGE
        );
    }


    private void addDetailRow(
            JPanel panel,
            GridBagConstraints gbc,
            String label,
            String value) {

        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setOpaque(false);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        nameLabel.setForeground(MUTED);
        nameLabel.setPreferredSize(new Dimension(105, 22));

        JLabel valueLabel = new JLabel(
                value == null || value.trim().isEmpty()
                        ? "Not available"
                        : value
        );

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        valueLabel.setForeground(DARK);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);

        panel.add(row, gbc);
        gbc.gridy++;
    }

    private void showBookingDialog() {

        try {

            List<Map<String, Object>> patients =
                    patientDAO.getAllPatients();

            List<Map<String, Object>> doctors =
                    appointmentDAO.getAllDoctors();

            List<Map<String, Object>> nurses =
                    appointmentDAO.getAllNurses();

            if (patients.isEmpty()
                    || doctors.isEmpty()
                    || nurses.isEmpty()) {

                showError(
                        "Patients, doctors, or nurses are unavailable.");

                return;
            }

            JComboBox<PatientOption> patientBox =
                    createPatientCombo(patients);

            JComboBox<StaffOption> doctorBox =
                    createStaffCombo(doctors);

            JComboBox<StaffOption> nurseBox =
                    createStaffCombo(nurses);

            JTextField dateField = new JTextField(
                    LocalDate.now().format(DATE_FORMAT), 12);

            JTextField timeField =
                    new JTextField("09:00", 12);

            JTextArea reasonArea =
                    new JTextArea(3, 22);

            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            reasonArea.setFont(
                    new Font("SansSerif", Font.PLAIN, 12));

            if ("Doctor".equals(currentRole)) {

                selectStaff(doctorBox, currentStaffId);
                doctorBox.setEnabled(false);

            } else {

                selectStaff(nurseBox, currentStaffId);
                nurseBox.setEnabled(false);
            }

            JPanel form = createAppointmentForm();

            addFormRow(form, 0, "Patient", patientBox);
            addFormRow(form, 1, "Doctor", doctorBox);
            addFormRow(form, 2, "Nurse", nurseBox);

            addFormRow(
                    form, 3,
                    "Date (YYYY-MM-DD)",
                    dateField);

            addFormRow(
                    form, 4,
                    "Time (HH:MM)",
                    timeField);

            addFormRow(
                    form, 5,
                    "Reason",
                    new JScrollPane(reasonArea));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Book New Appointment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            PatientOption patient =
                    (PatientOption) patientBox.getSelectedItem();

            StaffOption doctor =
                    (StaffOption) doctorBox.getSelectedItem();

            StaffOption nurse =
                    (StaffOption) nurseBox.getSelectedItem();

            if (patient == null
                    || doctor == null
                    || nurse == null) {

                showError(
                        "Please select a patient, doctor, and nurse.");

                return;
            }

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim(),
                    DATE_FORMAT);

            LocalTime time = LocalTime.parse(
                    timeField.getText().trim(),
                    TIME_FORMAT);

            String message = appointmentDAO.bookAppointment(
                    patient.id,
                    currentStaffId,
                    currentRole,
                    doctor.id,
                    nurse.id,
                    date,
                    time,
                    reasonArea.getText().trim()
            );

            if ("Appointment booked".equalsIgnoreCase(message)) {

                showSuccess(
                        "Appointment booked successfully!");

                loadAppointments();

            } else {

                showError(message);
            }

        } catch (DateTimeParseException exception) {

            showError(
                    "Invalid date or time.\n\n"
                            + "Date format: YYYY-MM-DD\n"
                            + "Time format: HH:MM");

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to book appointment:\n"
                            + exception.getMessage());
        }
    }

    private void showEditDialog(
            Map<String, Object> appointment) {

        try {

            List<Map<String, Object>> patients =
                    patientDAO.getAllPatients();

            List<Map<String, Object>> doctors =
                    appointmentDAO.getAllDoctors();

            List<Map<String, Object>> nurses =
                    appointmentDAO.getAllNurses();

            JComboBox<PatientOption> patientBox =
                    createPatientCombo(patients);

            JComboBox<StaffOption> doctorBox =
                    createStaffCombo(doctors);

            JComboBox<StaffOption> nurseBox =
                    createStaffCombo(nurses);

            selectPatient(
                    patientBox,
                    getInt(appointment, "patient_id"));

            selectStaff(
                    doctorBox,
                    getInt(appointment, "doctor_id"));

            selectStaff(
                    nurseBox,
                    getInt(appointment, "nurse_id"));

            if ("Doctor".equals(currentRole)) {

                selectStaff(doctorBox, currentStaffId);
                doctorBox.setEnabled(false);

            } else {

                selectStaff(nurseBox, currentStaffId);
                nurseBox.setEnabled(false);
            }

            LocalDate existingDate =
                    getDate(appointment.get("appointment_date"));

            LocalTime existingTime =
                    getTime(appointment.get("appointment_time"));

            JTextField dateField = new JTextField(
                    existingDate == null
                            ? ""
                            : existingDate.format(DATE_FORMAT),
                    12);

            JTextField timeField = new JTextField(
                    existingTime == null
                            ? ""
                            : existingTime.format(TIME_FORMAT),
                    12);

            JTextArea reasonArea = new JTextArea(
                    getString(appointment, "reason"),
                    3,
                    22);

            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            JPanel form = createAppointmentForm();

            addFormRow(form, 0, "Patient", patientBox);
            addFormRow(form, 1, "Doctor", doctorBox);
            addFormRow(form, 2, "Nurse", nurseBox);

            addFormRow(
                    form, 3,
                    "Date (YYYY-MM-DD)",
                    dateField);

            addFormRow(
                    form, 4,
                    "Time (HH:MM)",
                    timeField);

            addFormRow(
                    form, 5,
                    "Reason",
                    new JScrollPane(reasonArea));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Edit Appointment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            PatientOption patient =
                    (PatientOption) patientBox.getSelectedItem();

            StaffOption doctor =
                    (StaffOption) doctorBox.getSelectedItem();

            StaffOption nurse =
                    (StaffOption) nurseBox.getSelectedItem();

            if (patient == null
                    || doctor == null
                    || nurse == null) {

                showError(
                        "Please select all required staff and patient details.");

                return;
            }

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim(),
                    DATE_FORMAT);

            LocalTime time = LocalTime.parse(
                    timeField.getText().trim(),
                    TIME_FORMAT);

            boolean updated =
                    appointmentDAO.updateAppointment(
                            getInt(appointment, "appointment_id"),
                            currentStaffId,
                            currentRole,
                            patient.id,
                            doctor.id,
                            nurse.id,
                            date,
                            time,
                            reasonArea.getText().trim()
                    );

            if (updated) {

                showSuccess(
                        "Appointment updated successfully!");

                loadAppointments();

            } else {

                showError(
                        "Appointment could not be updated. "
                                + "It may no longer be assigned to your account.");
            }

        } catch (DateTimeParseException exception) {

            showError(
                    "Invalid date or time.\n\n"
                            + "Date format: YYYY-MM-DD\n"
                            + "Time format: HH:MM");

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to update appointment:\n"
                            + exception.getMessage());
        }
    }

    private JPanel createAppointmentForm() {

        JPanel panel = new JPanel(
                new GridBagLayout());

        panel.setBackground(WHITE);

        panel.setBorder(
                new EmptyBorder(16, 17, 16, 17));

        return panel;
    }

    private void addFormRow(
            JPanel panel,
            int row,
            String label,
            JComponent component) {

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(8, 5, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(
                new Font("SansSerif", Font.BOLD, 11));

        fieldLabel.setForeground(TEXT);

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.insets = new Insets(8, 5, 8, 5);

        component.setFont(
                new Font("SansSerif", Font.PLAIN, 12));

        if (component instanceof JTextField) {

            component.setBorder(
                    BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER, 1, true),
                            new EmptyBorder(7, 9, 7, 9)));

        } else if (component instanceof JComboBox) {

            component.setBackground(WHITE);

        } else if (component instanceof JScrollPane) {

            component.setBorder(
                    new LineBorder(BORDER, 1, true));
        }

        panel.add(component, gbc);
    }

    private JComboBox<PatientOption> createPatientCombo(
            List<Map<String, Object>> patients) {

        JComboBox<PatientOption> combo =
                new JComboBox<>();

        for (Map<String, Object> patient : patients) {

            combo.addItem(new PatientOption(
                    getInt(patient, "patient_id"),
                    getString(patient, "name")
            ));
        }

        return combo;
    }

    private JComboBox<StaffOption> createStaffCombo(
            List<Map<String, Object>> staffList) {

        JComboBox<StaffOption> combo =
                new JComboBox<>();

        for (Map<String, Object> staff : staffList) {

            combo.addItem(new StaffOption(
                    getInt(staff, "staff_id"),
                    getString(staff, "name")
            ));
        }

        return combo;
    }

    private void selectPatient(
            JComboBox<PatientOption> combo,
            int patientId) {

        for (int i = 0; i < combo.getItemCount(); i++) {

            PatientOption option =
                    combo.getItemAt(i);

            if (option.id == patientId) {

                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectStaff(
            JComboBox<StaffOption> combo,
            int staffId) {

        for (int i = 0; i < combo.getItemCount(); i++) {

            StaffOption option =
                    combo.getItemAt(i);

            if (option.id == staffId) {

                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cancelAppointment(int appointmentId) {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this appointment?",
                "Cancel Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        changeStatus(
                appointmentId,
                "Cancelled");
    }

    private void changeStatus(
            int appointmentId,
            String status) {

        try {

            boolean updated =
                    appointmentDAO.updateAppointmentStatus(
                            appointmentId,
                            currentStaffId,
                            currentRole,
                            status
                    );

            if (updated) {

                showSuccess(
                        "Appointment marked as " + status + ".");

                loadAppointments();

            } else {

                showError(
                        "Appointment status could not be updated.");
            }

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to update appointment:\n"
                            + exception.getMessage());
        }
    }

    private JButton createPrimaryButton(String text) {

        JButton button = new JButton(text);

        button.setUI(
                new javax.swing.plaf.basic.BasicButtonUI());

        button.setFont(
                new Font("SansSerif", Font.BOLD, 11));

        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(
                new EmptyBorder(11, 17, 11, 17));

        return button;
    }

    private JButton createSecondaryButton(String text) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 11));

        button.setForeground(TEAL);
        button.setBackground(WHITE);
        button.setFocusPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER, 1, true),
                        new EmptyBorder(9, 13, 9, 13)));

        return button;
    }

    private String normalizeRole(String role) {

        if (role == null) {

            throw new IllegalArgumentException(
                    "Staff role is missing.");
        }

        if (role.equalsIgnoreCase("Doctor")) {
            return "Doctor";
        }

        if (role.equalsIgnoreCase("Nurse")) {
            return "Nurse";
        }

        throw new IllegalArgumentException(
                "Appointments are not available for role: " + role
        );
    }

    private int getInt(
            Map<String, Object> row,
            String key) {

        Object value = row.get(key);

        if (value instanceof Number) {

            return ((Number) value).intValue();
        }

        return Integer.parseInt(
                String.valueOf(value));
    }

    private String getString(
            Map<String, Object> row,
            String key) {

        Object value = row.get(key);

        return value == null
                ? ""
                : String.valueOf(value);
    }

    private LocalDate getDate(Object value) {

        if (value instanceof LocalDate) {

            return (LocalDate) value;
        }

        if (value instanceof java.sql.Date) {

            return ((java.sql.Date) value).toLocalDate();
        }

        if (value instanceof String) {

            return LocalDate.parse((String) value);
        }

        return null;
    }

    private LocalTime getTime(Object value) {

        if (value instanceof LocalTime) {

            return (LocalTime) value;
        }

        if (value instanceof java.sql.Time) {

            return ((java.sql.Time) value).toLocalTime();
        }

        if (value instanceof String) {

            return LocalTime.parse((String) value);
        }

        return null;
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void showSuccess(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "CLINOVA",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "CLINOVA",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class PatientOption {

        private final int id;
        private final String name;

        private PatientOption(
                int id,
                String name) {

            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {

            return name + " (ID: " + id + ")";
        }
    }

    private static class StaffOption {

        private final int id;
        private final String name;

        private StaffOption(
                int id,
                String name) {

            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
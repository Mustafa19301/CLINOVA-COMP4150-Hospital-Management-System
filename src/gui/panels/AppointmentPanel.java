package gui.panels;

import dao.AppointmentDAO;
import dao.PatientDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
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

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final int currentStaffId;
    private final String currentStaffName;
    private final String currentRole;

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;

    private List<Map<String, Object>> appointments =
            new ArrayList<>();

    private JPanel agendaPanel;
    private JPanel summaryPanel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel appointmentCountLabel;
    private JLabel emptyLabel;

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
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appointment Scheduler");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Coordinate patient visits, manage your schedule, and keep appointments on track.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JButton bookButton = createPrimaryButton("+  Book Appointment");
        bookButton.addActionListener(e -> showBookingDialog());

        JButton refreshButton = createSecondaryButton("↻  Refresh");
        refreshButton.addActionListener(e -> loadAppointments());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(bookButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JPanel createSummaryPanel() {
        // Keep all three statistics together in one shared summary strip.
        JPanel panel = new JPanel(new GridLayout(1, 3, 0, 0));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(13, 12, 13, 12)));

        panel.add(createSummaryCard(
                "SCHEDULED", "0", TEAL,
                new Color(232, 243, 240)));

        panel.add(createSummaryCard(
                "COMPLETED", "0", BLUE,
                new Color(235, 240, 248)));

        panel.add(createSummaryCard(
                "CANCELLED", "0", RED,
                new Color(249, 237, 237)));

        return panel;
    }

    private JPanel createSummaryCard(
            String label,
            String value,
            Color accent,
            Color lightBackground) {

        JPanel segment = new JPanel(new BorderLayout(12, 0));
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
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setBackground(BACKGROUND);

        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(BACKGROUND);

        JPanel sectionTitle = new JPanel();
        sectionTitle.setOpaque(false);
        sectionTitle.setLayout(new BoxLayout(
                sectionTitle, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Appointment Agenda");
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "A clear view of the appointments assigned to your account");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        sectionTitle.add(title);
        sectionTitle.add(Box.createVerticalStrut(4));
        sectionTitle.add(subtitle);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filters.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(185, 36));
        searchField.setToolTipText("Search by patient name");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(0, 10, 0, 10)));

        searchField.addActionListener(e -> loadAppointments());

        JButton searchButton = createSecondaryButton("Search");
        searchButton.addActionListener(e -> loadAppointments());

        statusFilter = new JComboBox<>(new String[]{
                "All Statuses",
                "Scheduled",
                "Completed",
                "Cancelled"
        });

        statusFilter.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusFilter.setPreferredSize(new Dimension(138, 36));
        statusFilter.setBackground(WHITE);
        statusFilter.addActionListener(e -> renderAgenda());

        filters.add(searchField);
        filters.add(searchButton);
        filters.add(statusFilter);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(filters, BorderLayout.EAST);

        agendaPanel = new JPanel();
        agendaPanel.setLayout(new BoxLayout(
                agendaPanel, BoxLayout.Y_AXIS));
        agendaPanel.setBackground(BACKGROUND);
        agendaPanel.setBorder(new EmptyBorder(2, 1, 12, 1));

        JScrollPane scrollPane = new JScrollPane(agendaPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(
                new Dimension(8, 0));

        appointmentCountLabel = new JLabel("0 appointments");
        appointmentCountLabel.setFont(
                new Font("SansSerif", Font.BOLD, 10));
        appointmentCountLabel.setForeground(MUTED);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 2, 0, 2));
        footer.add(appointmentCountLabel, BorderLayout.WEST);

        section.add(toolbar, BorderLayout.NORTH);
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
                    "Unable to load appointments:\n" +
                            exception.getMessage()
            );
        }
    }

    private void updateSummary() {

        int scheduled = 0;
        int completed = 0;
        int cancelled = 0;

        for (Map<String, Object> appointment : appointments) {

            String status = getString(appointment, "status");

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

    private void updateSummaryValue(int index, String value) {

        if (summaryPanel == null) {
            return;
        }

        JPanel card = (JPanel) summaryPanel.getComponent(index);

        JLabel label = (JLabel) card.getClientProperty("countLabel");

        if (label != null) {
            label.setText(value);
        }
    }

    private void renderAgenda() {

        if (agendaPanel == null) {
            return;
        }

        agendaPanel.removeAll();

        String selectedStatus =
                statusFilter == null
                        ? "All Statuses"
                        : (String) statusFilter.getSelectedItem();

        Map<LocalDate, List<Map<String, Object>>> grouped =
                new LinkedHashMap<>();

        int visibleCount = 0;

        for (Map<String, Object> appointment : appointments) {

            String status = getString(appointment, "status");

            if (!"All Statuses".equals(selectedStatus)
                    && !selectedStatus.equals(status)) {
                continue;
            }

            LocalDate date = getDate(
                    appointment.get("appointment_date")
            );

            if (date == null) {
                continue;
            }

            grouped.computeIfAbsent(
                    date, key -> new ArrayList<>()
            ).add(appointment);

            visibleCount++;
        }

        if (grouped.isEmpty()) {

            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(WHITE);
            emptyPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(42, 20, 42, 20)
            ));

            emptyPanel.setLayout(new BoxLayout(
                    emptyPanel, BoxLayout.Y_AXIS));

            JLabel icon = new JLabel("▦");
            icon.setFont(new Font("SansSerif", Font.PLAIN, 30));
            icon.setForeground(TEAL);
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel message = new JLabel("No appointments found");
            message.setFont(new Font("SansSerif", Font.BOLD, 16));
            message.setForeground(DARK);
            message.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel hint = new JLabel(
                    "Your agenda is clear. Try adjusting the filters or book a new visit.");
            hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
            hint.setForeground(MUTED);
            hint.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(icon);
            emptyPanel.add(Box.createVerticalStrut(10));
            emptyPanel.add(message);
            emptyPanel.add(Box.createVerticalStrut(6));
            emptyPanel.add(hint);

            agendaPanel.add(emptyPanel);

        } else {

            for (Map.Entry<LocalDate,
                    List<Map<String, Object>>> entry
                    : grouped.entrySet()) {

                agendaPanel.add(createDateHeader(entry.getKey()));
                agendaPanel.add(Box.createVerticalStrut(10));

                for (Map<String, Object> appointment
                        : entry.getValue()) {

                    agendaPanel.add(createAppointmentCard(appointment));
                    agendaPanel.add(Box.createVerticalStrut(10));
                }

                agendaPanel.add(Box.createVerticalStrut(10));
            }
        }

        appointmentCountLabel.setText(
                visibleCount + (visibleCount == 1
                        ? " appointment"
                        : " appointments")
        );

        agendaPanel.revalidate();
        agendaPanel.repaint();
    }

    private JPanel createDateHeader(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 3, 8, 3));

        JLabel label = new JLabel(date.format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(DARK);

        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel schedule = new JLabel("DAILY SCHEDULE");
        schedule.setFont(new Font("SansSerif", Font.BOLD, 8));
        schedule.setForeground(MUTED);
        right.add(schedule);

        if (date.equals(LocalDate.now())) {
            JLabel today = new JLabel("TODAY");
            today.setFont(new Font("SansSerif", Font.BOLD, 9));
            today.setForeground(TEAL);
            today.setOpaque(true);
            today.setBackground(LIGHT_TEAL);
            today.setBorder(new EmptyBorder(4, 8, 4, 8));
            right.add(today);
        }

        panel.add(label, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JPanel createAppointmentCard(
            Map<String, Object> appointment) {

        JPanel card = new JPanel(new BorderLayout(17, 0));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(13, 15, 13, 15)));

        LocalTime time = getTime(
                appointment.get("appointment_time"));

        JPanel timePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(LIGHT_TEAL);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 13, 13);

                g2.dispose();
            }
        };

        timePanel.setOpaque(false);
        timePanel.setPreferredSize(new Dimension(96, 76));
        timePanel.setLayout(new BoxLayout(
                timePanel, BoxLayout.Y_AXIS));

        JLabel timeLabel = new JLabel(
                time == null
                        ? "--:--"
                        : time.format(
                        DateTimeFormatter.ofPattern("h:mm a")));

        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        timeLabel.setForeground(TEAL);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timeCaption = new JLabel("VISIT TIME");
        timeCaption.setFont(new Font("SansSerif", Font.BOLD, 8));
        timeCaption.setForeground(MUTED);
        timeCaption.setAlignmentX(Component.CENTER_ALIGNMENT);

        timePanel.add(Box.createVerticalGlue());
        timePanel.add(timeLabel);
        timePanel.add(Box.createVerticalStrut(5));
        timePanel.add(timeCaption);
        timePanel.add(Box.createVerticalGlue());

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(
                details, BoxLayout.Y_AXIS));

        JLabel patientName = new JLabel(
                getString(appointment, "patient_name"));

        patientName.setFont(
                new Font("SansSerif", Font.BOLD, 15));
        patientName.setForeground(DARK);

        String reasonText = getString(
                appointment, "reason").isEmpty()
                ? "No reason provided"
                : getString(appointment, "reason");

        JLabel reason = new JLabel(reasonText);
        reason.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reason.setForeground(TEXT);

        JLabel staff = new JLabel(
                "DOCTOR  "
                        + getString(appointment, "doctor_name")
                        + "     |     NURSE  "
                        + getString(appointment, "nurse_name"));

        staff.setFont(new Font("SansSerif", Font.PLAIN, 10));
        staff.setForeground(MUTED);

        details.add(Box.createVerticalGlue());
        details.add(patientName);
        details.add(Box.createVerticalStrut(6));
        details.add(reason);
        details.add(Box.createVerticalStrut(8));
        details.add(staff);
        details.add(Box.createVerticalGlue());

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(
                rightPanel, BoxLayout.Y_AXIS));

        String status = getString(appointment, "status");

        JLabel statusLabel = createStatusLabel(status);

        JPanel statusPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttons.setOpaque(false);

        int appointmentId = getInt(
                appointment, "appointment_id");

        if ("Scheduled".equalsIgnoreCase(status)) {

            JButton editButton = createSmallButton("Edit");
            editButton.addActionListener(
                    e -> showEditDialog(appointment));

            JButton completeButton = createSmallButton("Complete");
            completeButton.setForeground(GREEN);
            completeButton.addActionListener(e ->
                    changeStatus(appointmentId, "Completed"));

            JButton cancelButton = createSmallButton("Cancel");
            cancelButton.setForeground(RED);
            cancelButton.addActionListener(e ->
                    cancelAppointment(appointmentId));

            buttons.add(editButton);
            buttons.add(completeButton);
            buttons.add(cancelButton);
        }

        rightPanel.add(statusPanel);
        rightPanel.add(Box.createVerticalGlue());

        if ("Scheduled".equalsIgnoreCase(status)) {
            rightPanel.add(buttons);
        }

        card.add(timePanel, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 112));
        card.setPreferredSize(new Dimension(900, 108));

        return card;
    }

    private JLabel createStatusLabel(String status) {

        Color foreground;
        Color background;

        if ("Completed".equalsIgnoreCase(status)) {
            foreground = BLUE;
            background = new Color(235, 240, 248);

        } else if ("Cancelled".equalsIgnoreCase(status)) {
            foreground = RED;
            background = new Color(249, 237, 237);

        } else {
            foreground = GREEN;
            background = new Color(232, 243, 240);
        }

        JLabel label = new JLabel(status.toUpperCase());
        label.setFont(new Font("SansSerif", Font.BOLD, 9));
        label.setForeground(foreground);
        label.setOpaque(true);
        label.setBackground(background);
        label.setBorder(new EmptyBorder(5, 10, 5, 10));

        return label;
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
                        "Patients, doctors, or nurses are unavailable."
                );
                return;
            }

            JComboBox<PatientOption> patientBox =
                    createPatientCombo(patients);

            JComboBox<StaffOption> doctorBox =
                    createStaffCombo(doctors);

            JComboBox<StaffOption> nurseBox =
                    createStaffCombo(nurses);

            JTextField dateField = new JTextField(
                    LocalDate.now().format(DATE_FORMAT), 12
            );

            JTextField timeField = new JTextField("09:00", 12);

            JTextArea reasonArea = new JTextArea(3, 22);
            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);
            reasonArea.setFont(
                    new Font("SansSerif", Font.PLAIN, 12)
            );

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
            addFormRow(form, 3, "Date (YYYY-MM-DD)", dateField);
            addFormRow(form, 4, "Time (HH:MM)", timeField);
            addFormRow(form, 5, "Reason", new JScrollPane(reasonArea));

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

            if (patient == null || doctor == null || nurse == null) {
                showError(
                        "Please select a patient, doctor, and nurse.");
                return;
            }

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim(), DATE_FORMAT
            );

            LocalTime time = LocalTime.parse(
                    timeField.getText().trim(), TIME_FORMAT
            );

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

                showSuccess("Appointment booked successfully!");
                loadAppointments();

            } else {

                showError(message);
            }

        } catch (DateTimeParseException exception) {

            showError(
                    "Invalid date or time.\n\n" +
                            "Date format: YYYY-MM-DD\n" +
                            "Time format: HH:MM"
            );

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to book appointment:\n" +
                            exception.getMessage()
            );
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
                    getInt(appointment, "patient_id")
            );

            selectStaff(
                    doctorBox,
                    getInt(appointment, "doctor_id")
            );

            selectStaff(
                    nurseBox,
                    getInt(appointment, "nurse_id")
            );

            if ("Doctor".equals(currentRole)) {

                selectStaff(doctorBox, currentStaffId);
                doctorBox.setEnabled(false);

            } else {

                selectStaff(nurseBox, currentStaffId);
                nurseBox.setEnabled(false);
            }

            LocalDate existingDate = getDate(
                    appointment.get("appointment_date")
            );

            LocalTime existingTime = getTime(
                    appointment.get("appointment_time")
            );

            JTextField dateField = new JTextField(
                    existingDate == null
                            ? ""
                            : existingDate.format(DATE_FORMAT),
                    12
            );

            JTextField timeField = new JTextField(
                    existingTime == null
                            ? ""
                            : existingTime.format(TIME_FORMAT),
                    12
            );

            JTextArea reasonArea = new JTextArea(
                    getString(appointment, "reason"),
                    3,
                    22
            );

            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            JPanel form = createAppointmentForm();

            addFormRow(form, 0, "Patient", patientBox);
            addFormRow(form, 1, "Doctor", doctorBox);
            addFormRow(form, 2, "Nurse", nurseBox);
            addFormRow(form, 3, "Date (YYYY-MM-DD)", dateField);
            addFormRow(form, 4, "Time (HH:MM)", timeField);
            addFormRow(
                    form, 5, "Reason",
                    new JScrollPane(reasonArea)
            );

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

            if (patient == null || doctor == null || nurse == null) {
                showError(
                        "Please select all required staff and patient details.");
                return;
            }

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim(), DATE_FORMAT
            );

            LocalTime time = LocalTime.parse(
                    timeField.getText().trim(), TIME_FORMAT
            );

            boolean updated = appointmentDAO.updateAppointment(
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

                showSuccess("Appointment updated successfully!");
                loadAppointments();

            } else {

                showError(
                        "Appointment could not be updated. " +
                                "It may no longer be assigned to your account."
                );
            }

        } catch (DateTimeParseException exception) {

            showError(
                    "Invalid date or time.\n\n" +
                            "Date format: YYYY-MM-DD\n" +
                            "Time format: HH:MM"
            );

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to update appointment:\n" +
                            exception.getMessage()
            );
        }
    }

    private JPanel createAppointmentForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(16, 17, 16, 17));
        return panel;
    }

    private void addFormRow(
            JPanel panel,
            int row,
            String label,
            JComponent component) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 5, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        fieldLabel.setForeground(TEXT);

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        component.setFont(
                new Font("SansSerif", Font.PLAIN, 12));

        if (component instanceof JTextField) {
            component.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(7, 9, 7, 9)));

        } else if (component instanceof JComboBox) {
            component.setBackground(WHITE);

        } else if (component instanceof JScrollPane) {
            component.setBorder(new LineBorder(BORDER, 1, true));
        }

        panel.add(component, gbc);
    }

    private JComboBox<PatientOption> createPatientCombo(
            List<Map<String, Object>> patients) {

        JComboBox<PatientOption> combo = new JComboBox<>();

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

        JComboBox<StaffOption> combo = new JComboBox<>();

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

            PatientOption option = combo.getItemAt(i);

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

            StaffOption option = combo.getItemAt(i);

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

        changeStatus(appointmentId, "Cancelled");
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
                        "Appointment marked as " + status + "."
                );

                loadAppointments();

            } else {

                showError(
                        "Appointment status could not be updated."
                );
            }

        } catch (SQLException | IllegalArgumentException exception) {

            showError(
                    "Unable to update appointment:\n" +
                            exception.getMessage()
            );
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 17, 11, 17));

        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setForeground(TEAL);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(9, 13, 9, 13)));

        return button;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 9));
        button.setForeground(TEAL);
        button.setBackground(WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 9, 6, 9)));
        button.setMargin(new Insets(2, 4, 2, 4));

        return button;
    }

    private String normalizeRole(String role) {

        if (role == null) {
            throw new IllegalArgumentException("Staff role is missing.");
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

        return Integer.parseInt(String.valueOf(value));
    }

    private String getString(
            Map<String, Object> row,
            String key) {

        Object value = row.get(key);

        return value == null ? "" : String.valueOf(value);
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

    private void showSuccess(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "CarePoint",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "CarePoint",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class PatientOption {

        private final int id;
        private final String name;

        private PatientOption(int id, String name) {
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

        private StaffOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
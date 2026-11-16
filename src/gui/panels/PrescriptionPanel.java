package gui.panels;

import dao.PatientDAO;
import dao.PrescriptionDAO;
import model.Prescription;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;

public class PrescriptionPanel extends JPanel {

    private static final Color DARK = new Color(16, 45, 56);
    private static final Color TEAL = new Color(23, 107, 104);
    private static final Color BACKGROUND = new Color(243, 247, 246);
    private static final Color TEXT = new Color(35, 58, 62);
    private static final Color MUTED = new Color(112, 132, 134);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER = new Color(225, 233, 231);

    private final int currentStaffId;
    private final String currentStaffName;
    private final String currentRole;

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private JTable prescriptionTable;
    private DefaultTableModel tableModel;

    public PrescriptionPanel(int staffId, String staffName, String role) {

        this.currentStaffId = staffId;
        this.currentStaffName = staffName;
        this.currentRole = role;

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        loadPrescriptions();
    }

    //Table
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 15, 14, 15)
        ));

        JLabel heading = new JLabel("Prescriptions");
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(DARK);

        String[] columns = {"ID", "Date", "Patient", "Doctor", "Medication", "Pharmacist", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        prescriptionTable = new JTable(tableModel);
        prescriptionTable.setRowHeight(34);
        prescriptionTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        prescriptionTable.setForeground(TEXT);
        prescriptionTable.setSelectionBackground(new Color(218, 239, 235));
        prescriptionTable.setSelectionForeground(DARK);
        prescriptionTable.setGridColor(BORDER);
        prescriptionTable.setShowVerticalLines(false);
        prescriptionTable.setFillsViewportHeight(true);

        prescriptionTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        prescriptionTable.getTableHeader().setForeground(DARK);
        prescriptionTable.getTableHeader().setBackground(
                new Color(239, 245, 243)
        );
        prescriptionTable.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    //Load all prescriptions
    private void loadPrescriptions() {
        try {
            List<Map<String, Object>> rows = prescriptionDAO.getAllPrescriptions();

            tableModel.setRowCount(0);

            for (Map<String, Object> row : rows) {
                tableModel.addRow(new Object[] {
                        row.get("prescription_id"),
                        row.get("date"),
                        row.get("patient_name"),
                        row.get("doctor_name"),
                        row.get("medication_name"),
                        row.get("pharmacist_name"),
                        row.get("status")
                });
            }

        } catch (Exception exception) {
            showError("Unable to load prescriptions.", exception);
        }
    }

    //Button panel
    private JPanel createToolbar() {

        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel userLabel = new JLabel(currentRole + ": " + currentStaffName);
        userLabel.setForeground(TEXT);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton newButton = createButton("New Prescription", TEAL);
        JButton dispenseButton = createButton("Dispense", TEAL);
        JButton cancelButton = createButton("Cancel", new Color(180, 65, 65));
        JButton refreshButton = createButton("Refresh", new Color(85, 105, 108));

        newButton.addActionListener(e -> addPrescription());
        dispenseButton.addActionListener(e -> dispenseSelected());
        cancelButton.addActionListener(e -> cancelSelected());
        refreshButton.addActionListener(e -> loadPrescriptions());

        if (currentRole.equals("Doctor")) {
            right.add(newButton);
        }
        if (currentRole.equals("PharmacyStaff")) {
            right.add(dispenseButton);
        }
        right.add(cancelButton);
        right.add(refreshButton);

        panel.add(userLabel, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    //Add new prescription
    private void addPrescription() {

        JComboBox<Option> patientBox = new JComboBox<>();
        JComboBox<Option> medicationBox = new JComboBox<>();

        try {
            for (Map<String, Object> p : patientDAO.getAllPatients()) {
                patientBox.addItem(new Option(
                        ((Number) p.get("patient_id")).intValue(),
                        (String) p.get("name")));
            }
            for (Map<String, Object> m : prescriptionDAO.getAllMedications()) {
                medicationBox.addItem(new Option(
                        ((Number) m.get("medication_id")).intValue(),
                        (String) m.get("name")));
            }
        } catch (Exception exception) {
            showError("Unable to load patients or medications.", exception);
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Patient:"));
        form.add(patientBox);
        form.add(new JLabel("Medication:"));
        form.add(medicationBox);
        form.add(new JLabel("Doctor:"));
        form.add(new JLabel(currentStaffName));

        int choice = JOptionPane.showConfirmDialog(
                this, form, "New Prescription",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        Option patient = (Option) patientBox.getSelectedItem();
        Option medication = (Option) medicationBox.getSelectedItem();

        if (patient == null || medication == null) {
            JOptionPane.showMessageDialog(this, "No patients or medications available.");
            return;
        }

        try {
            Prescription prescription = new Prescription(
                    0,
                    patient.id,
                    currentStaffId,
                    medication.id,
                    0,
                    LocalDate.now(),
                    "Pending"
            );

            prescriptionDAO.addPrescription(prescription);
            loadPrescriptions();
        } catch (Exception exception) {
            showError("Unable to add prescription.", exception);
        }
    }

    //Dispense selected prescription
    private void dispenseSelected() {

        int row = prescriptionTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        int id = ((Number) tableModel.getValueAt(row, 0)).intValue();
        String status = (String) tableModel.getValueAt(row, 6);

        if (!status.equals("Pending")) {
            JOptionPane.showMessageDialog(this, "Only pending prescriptions can be dispensed.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Dispense prescription " + id + " for " + tableModel.getValueAt(row, 2) + "?\n"
                        + "This cannot be undone.",
                "Confirm Dispense", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!prescriptionDAO.dispensePrescription(id, currentStaffId)) {
                JOptionPane.showMessageDialog(this, "This prescription was already changed.");
            }
            loadPrescriptions();
        } catch (Exception exception) {
            showError("Unable to dispense prescription.", exception);
        }
    }

    //Cancel selected prescription
    private void cancelSelected() {

        int row = prescriptionTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        int id = ((Number) tableModel.getValueAt(row, 0)).intValue();
        String status = (String) tableModel.getValueAt(row, 6);

        if (!status.equals("Pending")) {
            JOptionPane.showMessageDialog(this, "Only pending prescriptions can be cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel prescription " + id + " for " + tableModel.getValueAt(row, 2) + "?\n"
                        + "This cannot be undone.",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!prescriptionDAO.cancelPrescription(id)) {
                JOptionPane.showMessageDialog(this, "This prescription was already changed.");
            }
            loadPrescriptions();
        } catch (Exception exception) {
            showError("Unable to cancel prescription.", exception);
        }
    }

    //Make a button
    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 13, 9, 13));

        return button;
    }

    //Show errors
    private void showError(String message, Exception exception) {

        exception.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    //Dropdown item
    private static class Option {

        private final int id;
        private final String name;

        private Option(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}

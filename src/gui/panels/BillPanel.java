
package gui.panels;

import dao.BillDAO;
import dao.PatientDAO;
import dao.PrescriptionDAO;
import model.Bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BillPanel extends JPanel {

    private static final Color DARK = new Color(16, 45, 56);
    private static final Color TEAL = new Color(23, 107, 104);
    private static final Color BACKGROUND = new Color(243, 247, 246);
    private static final Color TEXT = new Color(35, 58, 62);
    private static final Color MUTED = new Color(112, 132, 134);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER = new Color(225, 233, 231);

    private final BillDAO billDAO = new BillDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    private JTable billTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BillPanel() {

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadBills();
    }

   
    //table
  
    private JPanel createTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 15, 14, 15)
        ));

        JLabel heading = new JLabel("Patient Bills");
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(DARK);

        String[] columns = {
                "Bill ID",
                "Patient ID",
                "Prescription ID",
                "Amount",
                "Date",
                "Description",
                "Payment Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        billTable.setRowHeight(34);
        billTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        billTable.setForeground(TEXT);
        billTable.setSelectionBackground(
                new Color(218, 239, 235)
        );
        billTable.setSelectionForeground(DARK);
        billTable.setGridColor(BORDER);
        billTable.setShowVerticalLines(false);
        billTable.setFillsViewportHeight(true);
        billTable.setAutoCreateRowSorter(true);

        billTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        billTable.getTableHeader().setForeground(DARK);
        billTable.getTableHeader().setBackground(
                new Color(239, 245, 243)
        );

        billTable.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    
    //Toolbar

    private JPanel createToolbar() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Same top-row arrangement as PrescriptionPanel.
        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JLabel title = new JLabel("Billing Management");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addButton = createButton("New Bill", TEAL);
        JButton updateButton = createButton("Update Payment", TEAL);
        JButton deleteButton = createButton("Delete", new Color(180, 65, 65));
        JButton refreshButton = createButton("Refresh", new Color(85, 105, 108));

        addButton.addActionListener(e -> addBill());
        updateButton.addActionListener(e -> updateSelectedPayment());
        deleteButton.addActionListener(e -> deleteSelectedBill());
        refreshButton.addActionListener(e -> loadBills());

        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        topRow.add(title, BorderLayout.WEST);
        topRow.add(actions, BorderLayout.EAST);

        // Align patient search directly below the action buttons.
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRow.setOpaque(false);

        JLabel searchLabel = new JLabel("Patient ID:");
        searchLabel.setForeground(TEXT);
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        searchField = new JTextField(12);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton searchButton = createButton("Search", TEAL);
        JButton clearButton = createButton("Clear", new Color(85, 105, 108));

        searchButton.addActionListener(e -> searchBills());
        searchField.addActionListener(e -> searchBills());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadBills();
        });

        searchRow.add(searchLabel);
        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(clearButton);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(searchRow, BorderLayout.SOUTH);

        return panel;
    }


        //Load all bills
    

    private void loadBills() {

        try {

            List<Bill> bills = billDAO.getAllBills();

            tableModel.setRowCount(0);

            for (Bill bill : bills) {
                addBillRow(bill);
            }

        } catch (Exception exception) {

            showError("Unable to load bills.", exception);
        }
    }

    //search
    private void searchBills() {

        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            loadBills();
            return;
        }

        try {

            int patientId = Integer.parseInt(search);

            List<Bill> bills =
                    billDAO.getPatientBills(patientId);

            tableModel.setRowCount(0);

            for (Bill bill : bills) {
                addBillRow(bill);
            }

            if (bills.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No bills were found for patient ID "
                                + patientId + ".",
                        "Search Results",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (NumberFormatException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid numeric patient ID.",
                    "Invalid Search",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (Exception exception) {

            showError("Unable to search bills.", exception);
        }
    }

    private void addBillRow(Bill bill) {

        tableModel.addRow(new Object[] {
                bill.getBill_id(),
                bill.getPatient_id(),
                bill.getPrescription_id() == 0
                        ? "N/A"
                        : bill.getPrescription_id(),
                String.format("$%.2f", bill.getAmount()),
                bill.getDate(),
                bill.getDescription(),
                bill.getPayment_status()
        });
    }

    
    //Make new bill
    

    private void addBill() {

        JComboBox<Option> patientBox = new JComboBox<>();
        JComboBox<Option> prescriptionBox = new JComboBox<>();

        JTextField amountField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);

        try {

            
            for (Map<String, Object> patient
                    : patientDAO.getAllPatients()) {

                patientBox.addItem(new Option(
                        ((Number) patient.get("patient_id")).intValue(),
                        String.valueOf(patient.get("name"))
                ));
            }

            
            prescriptionBox.addItem(
                    new Option(0, "No prescription")
            );

            
            for (Map<String, Object> prescription
                    : prescriptionDAO.getAllPrescriptions()) {

                int prescriptionId =
                        ((Number) prescription.get("prescription_id"))
                                .intValue();

                String patientName =
                        String.valueOf(prescription.get("patient_name"));

                String medicationName =
                        String.valueOf(prescription.get("medication_name"));

                prescriptionBox.addItem(new Option(
                        prescriptionId,
                        "Prescription #" + prescriptionId
                                + " - " + patientName
                                + " (" + medicationName + ")"
                ));
            }

        } catch (Exception exception) {

            showError(
                    "Unable to load patients or prescriptions.",
                    exception
            );

            return;
        }

        JPanel form = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        form.add(new JLabel("Patient:"));
        form.add(patientBox);

        form.add(new JLabel("Prescription:"));
        form.add(prescriptionBox);

        form.add(new JLabel("Amount:"));
        form.add(amountField);

        form.add(new JLabel("Description:"));
        form.add(descriptionField);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Create New Bill",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        Option patient =
                (Option) patientBox.getSelectedItem();

        Option prescription =
                (Option) prescriptionBox.getSelectedItem();

        if (patient == null) {
            showError("Please select a patient.");
            return;
        }

        float amount;

        try {

            amount = Float.parseFloat(
                    amountField.getText().trim()
            );

            if (amount <= 0) {
                showError("The bill amount must be greater than zero.");
                return;
            }

        } catch (NumberFormatException exception) {

            showError("Please enter a valid bill amount.");
            return;
        }

        String description =
                descriptionField.getText().trim();

        if (description.isEmpty()) {
            showError("Please enter a bill description.");
            return;
        }

        int prescriptionId = prescription == null
                ? 0
                : prescription.id;

        Bill bill = new Bill(
                0,
                patient.id,
                prescriptionId,
                amount,
                LocalDate.now(),
                description,
                "Unpaid"
        );

        try {

            boolean created = billDAO.createBill(bill);

            if (created) {
                showSuccess("Bill created successfully.");
                loadBills();
            } else {
                showError("The bill could not be created.");
            }

        } catch (Exception exception) {

            showError("Unable to create bill.", exception);
        }
    }

    
    //Update payment status
    

    private void updateSelectedPayment() {

        int selectedRow = billTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a bill first.");
            return;
        }

        int modelRow =
                billTable.convertRowIndexToModel(selectedRow);

        int billId = ((Number)
                tableModel.getValueAt(modelRow, 0)).intValue();

        String currentStatus = String.valueOf(
                tableModel.getValueAt(modelRow, 6)
        );

        String[] statuses = {"Unpaid", "Paid"};

        JComboBox<String> statusBox =
                new JComboBox<>(statuses);

        statusBox.setSelectedItem(currentStatus);

        int choice = JOptionPane.showConfirmDialog(
                this,
                statusBox,
                "Update Payment Status",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String newStatus =
                String.valueOf(statusBox.getSelectedItem());

        try {

            boolean updated = billDAO.updatePaymentStatus(
                    billId,
                    newStatus
            );

            if (updated) {
                showSuccess("Payment status updated successfully.");
                refreshCurrentView();
            } else {
                showError("The payment status could not be updated.");
            }

        } catch (Exception exception) {

            showError(
                    "Unable to update payment status.",
                    exception
            );
        }
    }

    
    //Delete bill
    

    private void deleteSelectedBill() {

        int selectedRow = billTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select a bill first.");
            return;
        }

        int modelRow =
                billTable.convertRowIndexToModel(selectedRow);

        int billId = ((Number)
                tableModel.getValueAt(modelRow, 0)).intValue();

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete bill "
                        + billId + "?\nThis action cannot be undone.",
                "Confirm Bill Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            boolean deleted = billDAO.deleteBill(billId);

            if (deleted) {
                showSuccess("Bill deleted successfully.");
                refreshCurrentView();
            } else {
                showError("The bill could not be deleted.");
            }

        } catch (Exception exception) {

            showError("Unable to delete bill.", exception);
        }
    }

    
    //Refresh
    

    private void refreshCurrentView() {

        if (searchField.getText().trim().isEmpty()) {
            loadBills();
        } else {
            searchBills();
        }
    }

    
    //Buttons and messages
   

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        button.setForeground(WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                new EmptyBorder(9, 13, 9, 13)
        );

        return button;
    }

    private void showSuccess(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Billing",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showError(
            String message,
            Exception exception
    ) {

        exception.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    
    //Dropdown options
    

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

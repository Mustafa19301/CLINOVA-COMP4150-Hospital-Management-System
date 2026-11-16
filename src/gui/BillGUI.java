
package gui;

import dao.BillDAO;
import model.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BillGUI extends JFrame {

    private BillDAO dao = new BillDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField patientSearchField;

    private String currentView = "ALL";
    private int currentPatientId = 0;

    public BillGUI() {

        setTitle("Bill Management");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //Table
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

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);

        //Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        //Patient search
        patientSearchField = new JTextField(10);

        JButton searchButton = new JButton("Search Patient");
        JButton allButton = new JButton("Show All");
        JButton unpaidButton = new JButton("Unpaid Bills");
        JButton addButton = new JButton("Add Bill");
        JButton updateButton = new JButton("Update Status");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(new JLabel("Patient ID:"));
        buttonPanel.add(patientSearchField);
        buttonPanel.add(searchButton);

        buttonPanel.add(allButton);
        buttonPanel.add(unpaidButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        //Button actions
        searchButton.addActionListener(e -> searchPatient());

        
        patientSearchField.addActionListener(e -> searchPatient());

        allButton.addActionListener(e -> loadAll());
        unpaidButton.addActionListener(e -> loadUnpaid());
        addButton.addActionListener(e -> addBill());
        updateButton.addActionListener(e -> updateStatus());
        deleteButton.addActionListener(e -> deleteBill());
        refreshButton.addActionListener(e -> refreshTable());

        //Load bills when gui is opened
        loadAll();
    }

    //Load all bills
    private void loadAll() {

        currentView = "ALL";

        try {
            List<Bill> bills = dao.getAllBills();
            displayBills(bills);

        } catch (Exception e) {
            showError(e);
        }
    }

    //Load unpaid bills
    private void loadUnpaid() {

        currentView = "UNPAID";

        try {
            List<Bill> bills = dao.getUnpaidBills();
            displayBills(bills);

        } catch (Exception e) {
            showError(e);
        }
    }

    //Search bills by patient
    private void searchPatient() {

        String input = patientSearchField.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a Patient ID."
            );
            return;
        }

        try {

            currentPatientId = Integer.parseInt(input);

            if (currentPatientId <= 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid Patient ID."
                );
                return;
            }

            currentView = "SEARCH";

            List<Bill> bills =
                dao.getPatientBills(currentPatientId);

            displayBills(bills);

            if (bills.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "No bills found for Patient ID: " + currentPatientId
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid numeric Patient ID."
            );

        } catch (Exception e) {
            showError(e);
        }
    }

    //Show bills
    private void displayBills(List<Bill> bills) {

        tableModel.setRowCount(0);

        for (Bill bill : bills) {

            Object[] row = {
                bill.getBill_id(),
                bill.getPatient_id(),
                bill.getPrescription_id() == 0
                    ? "N/A" : bill.getPrescription_id(),
                bill.getAmount(),
                bill.getDate(),
                bill.getDescription(),
                bill.getPayment_status()
            };

            tableModel.addRow(row);
        }
    }

    //Add new bill
    private void addBill() {

        try {

            String patientInput = JOptionPane.showInputDialog(
                this, "Enter Patient ID:"
            );

            if (patientInput == null) return;

            String prescriptionInput = JOptionPane.showInputDialog(
                this,
                "Enter Prescription ID (enter 0 if none):"
            );

            if (prescriptionInput == null) return;

            String amountInput = JOptionPane.showInputDialog(
                this, "Enter Amount:"
            );

            if (amountInput == null) return;

            String dateInput = JOptionPane.showInputDialog(
                this,
                "Enter Date (YYYY-MM-DD):"
            );

            if (dateInput == null) return;

            String description = JOptionPane.showInputDialog(
                this, "Enter Description:"
            );

            if (description == null) return;

            String[] statuses = {"Unpaid", "Paid"};

            String status = (String) JOptionPane.showInputDialog(
                this,
                "Select Payment Status:",
                "Payment Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                "Unpaid"
            );

            if (status == null) return;

            int patientId = Integer.parseInt(patientInput.trim());
            int prescriptionId =
                Integer.parseInt(prescriptionInput.trim());

            float amount = Float.parseFloat(amountInput.trim());

            LocalDate date = LocalDate.parse(dateInput.trim());

            if (patientId <= 0 || prescriptionId < 0 || amount < 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid positive values."
                );
                return;
            }

            Bill bill = new Bill(
                0,
                patientId,
                prescriptionId,
                amount,
                date,
                description,
                status
            );

            if (dao.createBill(bill)) {

                JOptionPane.showMessageDialog(
                    this, "Bill added successfully!"
                );

                refreshTable();

            } else {
                JOptionPane.showMessageDialog(
                    this, "Failed to add bill."
                );
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    //Update payment status
    private void updateStatus() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a bill first."
            );
            return;
        }

        int billId = (int) tableModel.getValueAt(
            selectedRow, 0
        );

        String[] statuses = {"Unpaid", "Paid"};

        String status = (String) JOptionPane.showInputDialog(
            this,
            "Select the new payment status:",
            "Update Payment Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            "Paid"
        );

        if (status == null) return;

        try {

            if (dao.updatePaymentStatus(billId, status)) {

                JOptionPane.showMessageDialog(
                    this, "Payment status updated successfully!"
                );

                refreshTable();

            } else {
                JOptionPane.showMessageDialog(
                    this, "Bill could not be updated."
                );
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    //Delete bill
    private void deleteBill() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a bill first."
            );
            return;
        }

        int billId = (int) tableModel.getValueAt(
            selectedRow, 0
        );

        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete Bill ID " + billId + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (dao.deleteBill(billId)) {

                JOptionPane.showMessageDialog(
                    this, "Bill deleted successfully!"
                );

                refreshTable();

            } else {
                JOptionPane.showMessageDialog(
                    this, "Bill could not be deleted."
                );
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    //Refresh current view
    private void refreshTable() {

        switch (currentView) {

            case "ALL":
                loadAll();
                break;

            case "UNPAID":
                loadUnpaid();
                break;

            case "SEARCH":
                try {
                    List<Bill> bills =
                        dao.getPatientBills(currentPatientId);

                    displayBills(bills);

                } catch (Exception e) {
                    showError(e);
                }
                break;
        }
    }

    //Show errors
    private void showError(Exception e) {

        JOptionPane.showMessageDialog(
            this,
            "An error occurred: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );

        e.printStackTrace();
    }

    //Run gui
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new BillGUI().setVisible(true);
        });
    }
}

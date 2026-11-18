package gui.panels;

import dao.BillDAO;
import dao.PatientDAO;
import model.Bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
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

    private JTable billTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    private final Map<Integer, String> patientNames = new HashMap<>();

    public BillPanel() {

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        add(createToolbar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadBills();
    }

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
                "Patient Name",
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
        billTable.setSelectionBackground(new Color(218, 239, 235));
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

    private JPanel createToolbar() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JLabel title = new JLabel("Billing Management");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setOpaque(false);

        JButton updateButton = createButton("Update Payment", TEAL);
        JButton deleteButton = createButton(
                "Delete", new Color(180, 65, 65)
        );
        JButton refreshButton = createButton(
                "Refresh", new Color(85, 105, 108)
        );

        updateButton.addActionListener(e -> updateSelectedPayment());
        deleteButton.addActionListener(e -> deleteSelectedBill());
        refreshButton.addActionListener(e -> loadBills());

        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        topRow.add(title, BorderLayout.WEST);
        topRow.add(actions, BorderLayout.EAST);

        JPanel searchRow = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        searchRow.setOpaque(false);

        JLabel searchLabel = new JLabel("Patient ID:");
        searchLabel.setForeground(TEXT);
        searchLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        searchField = new JTextField(12);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton searchButton = createButton("Search", TEAL);
        JButton clearButton = createButton(
                "Clear", new Color(85, 105, 108)
        );

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

    private void loadPatientNames() throws Exception {

        patientNames.clear();

        for (Map<String, Object> patient : patientDAO.getAllPatients()) {

            int patientId = ((Number)
                    patient.get("patient_id")).intValue();

            String patientName = String.valueOf(
                    patient.get("name")
            );

            patientNames.put(patientId, patientName);
        }
    }

    private void loadBills() {

        try {

            loadPatientNames();

            List<Bill> bills = billDAO.getAllBills();

            tableModel.setRowCount(0);

            for (Bill bill : bills) {
                addBillRow(bill);
            }

        } catch (Exception exception) {

            showError("Unable to load bills.", exception);
        }
    }

    private void searchBills() {

        String search = searchField.getText().trim();

        if (search.isEmpty()) {
            loadBills();
            return;
        }

        try {

            int patientId = Integer.parseInt(search);
            loadPatientNames();
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

        String patientName = patientNames.getOrDefault(
                bill.getPatient_id(),
                "Unknown Patient"
        );

        tableModel.addRow(new Object[] {
                bill.getBill_id(),
                patientName,
                bill.getPrescription_id() == 0
                        ? "N/A"
                        : bill.getPrescription_id(),
                String.format("$%.2f", bill.getAmount()),
                bill.getDate(),
                bill.getDescription(),
                bill.getPayment_status()
        });
    }

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

        String patientName = String.valueOf(
                tableModel.getValueAt(modelRow, 1)
        );

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete bill "
                        + billId + " for " + patientName
                        + "?\nThis action cannot be undone.",
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

    private void refreshCurrentView() {

        if (searchField.getText().trim().isEmpty()) {
            loadBills();
        } else {
            searchBills();
        }
    }

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
}
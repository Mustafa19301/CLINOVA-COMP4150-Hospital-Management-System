package gui.panels;

import dao.PrescriptionDAO;

import javax.swing.*;
import gui.panels.PrescriptionPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    private JTable prescriptionTable;
    private DefaultTableModel tableModel;

    public PrescriptionPanel(int staffId, String staffName) {

        this.currentStaffId = staffId;
        this.currentStaffName = staffName;

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        add(createTablePanel(), BorderLayout.CENTER);
        loadPrescriptions();
    }

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

    private void showError(String message, Exception exception) {

        exception.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

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

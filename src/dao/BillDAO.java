
package dao;

import database.DatabaseConnection;
import model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    //Change row into bill object
    private Bill mapBill(ResultSet rs) throws SQLException {

        int prescriptionId = rs.getInt("prescription_id");

        if (rs.wasNull()) {
            prescriptionId = 0;
        }

        return new Bill(
            rs.getInt("bill_id"),
            rs.getInt("patient_id"),
            prescriptionId,
            rs.getFloat("amount"),
            rs.getDate("date").toLocalDate(),
            rs.getString("description"),
            rs.getString("payment_status")
        );
    }

    //Get all bills
    public List<Bill> getAllBills() throws SQLException {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM Bill ORDER BY `date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        }

        return bills;
    }

    //Get all unpaid bills
    public List<Bill> getUnpaidBills() throws SQLException {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM Bill " +
                     "WHERE payment_status = 'Unpaid' " +
                     "ORDER BY `date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        }

        return bills;
    }

    //Get all bills for a patient
    public List<Bill> getPatientBills(int patient_id)
            throws SQLException {

        List<Bill> bills = new ArrayList<>();

        String sql = "SELECT * FROM Bill " +
                     "WHERE patient_id = ? ORDER BY `date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patient_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapBill(rs));
                }
            }
        }

        return bills;
    }

    //Make a new bill
    public boolean createBill(Bill bill) throws SQLException {

        String sql = "INSERT INTO Bill " +
                     "(patient_id, prescription_id, amount, " +
                     "`date`, description, payment_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bill.getPatient_id());

            if (bill.getPrescription_id() == 0) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, bill.getPrescription_id());
            }

            stmt.setFloat(3, bill.getAmount());
            stmt.setDate(4, Date.valueOf(bill.getDate()));
            stmt.setString(5, bill.getDescription());
            stmt.setString(6, bill.getPayment_status());

            return stmt.executeUpdate() > 0;
        }
    }

    //Update payment status
    public boolean updatePaymentStatus(int bill_id, String status)
            throws SQLException {

        String sql = "UPDATE Bill SET payment_status = ? " +
                     "WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, bill_id);

            return stmt.executeUpdate() > 0;
        }
    }

    //Delete a bill
    public boolean deleteBill(int bill_id) throws SQLException {

        String sql = "DELETE FROM Bill WHERE bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bill_id);

            return stmt.executeUpdate() > 0;
        }
    }
}


package dao;

import database.DatabaseConnection;
import model.Prescription;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrescriptionDAO {

  
    //GET PRESCRIPTIONS FOR A SPECIFIC PATIENT
    public List<Prescription> getPatientPrescriptions(int patient_id)
            throws SQLException {

        List<Prescription> prescriptions = new ArrayList<>();

        String sql = "SELECT * FROM Prescription " +
                     "WHERE patient_id = ? ORDER BY `date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patient_id);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    int pharmacist_id = rs.getInt("pharmacist_id");

                    prescriptions.add(new Prescription(
                        rs.getInt("prescription_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("medication_id"),
                        rs.wasNull() ? 0 : pharmacist_id,
                        rs.getDate("date").toLocalDate(),
                        rs.getString("status")
                    ));
                }
            }
        }

        return prescriptions;
    }


   
    //GET ALL PENDING PRESCRIPTIONS
    public List<Prescription> getPendingPrescriptions()
            throws SQLException {

        List<Prescription> prescriptions = new ArrayList<>();

        String sql = "SELECT * FROM Prescription " +
                     "WHERE status = 'Pending' ORDER BY `date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                int pharmacist_id = rs.getInt("pharmacist_id");

                prescriptions.add(new Prescription(
                    rs.getInt("prescription_id"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getInt("medication_id"),
                    rs.wasNull() ? 0 : pharmacist_id,
                    rs.getDate("date").toLocalDate(),
                    rs.getString("status")
                ));
            }
        }

        return prescriptions;
    }


    
    //ADD A NEW PRESCRIPTION
    public boolean addPrescription(Prescription prescription)
            throws SQLException {

        String sql = "INSERT INTO Prescription " +
                     "(patient_id, doctor_id, medication_id, " +
                     "pharmacist_id, `date`, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescription.getPatient_id());
            stmt.setInt(2, prescription.getDoctor_id());
            stmt.setInt(3, prescription.getMedication_id());

            if (prescription.getPharmacist_id() == 0) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, prescription.getPharmacist_id());
            }

            stmt.setDate(5, Date.valueOf(prescription.getDate()));
            stmt.setString(6, prescription.getStatus_id());

            return stmt.executeUpdate() > 0;
        }
    }


    
    //UPDATE PRESCRIPTION STATUS
    public boolean updatePrescriptionStatus(
            int prescription_id, String status)
            throws SQLException {

        String sql = "UPDATE Prescription SET status = ? " +
                     "WHERE prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, prescription_id);

            return stmt.executeUpdate() > 0;
        }
    }

    //Get all prescriptions with names
    public List<Map<String, Object>> getAllPrescriptions()
            throws SQLException {

        String sql = "SELECT pr.prescription_id, " +
                "pr.`date`, " +
                "pt.name AS patient_name, " +
                "doc.name AS doctor_name, " +
                "m.name AS medication_name, " +
                "ph.name AS pharmacist_name, " +
                "pr.status " +
                "FROM Prescription pr " +
                "JOIN Patient pt ON pr.patient_id = pt.patient_id " +
                "JOIN Staff doc ON pr.doctor_id = doc.staff_id " +
                "JOIN Medication m ON pr.medication_id = m.medication_id " +
                "LEFT JOIN Staff ph ON pr.pharmacist_id = ph.staff_id " +
                "ORDER BY pr.`date` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return readRows(rs);
        }
    }

    //Get all medications for the dropdown
    public List<Map<String, Object>> getAllMedications()
            throws SQLException {

        String sql = "SELECT medication_id, name, price " +
                     "FROM Medication ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return readRows(rs);
        }
    }

    //Change rows into maps
    private List<Map<String, Object>> readRows(ResultSet rs)
            throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        ResultSetMetaData metadata = rs.getMetaData();
        int columnCount = metadata.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();

            for (int i = 1; i <= columnCount; i++) {

                Object value = rs.getObject(i);

                if (value instanceof Date) {
                    value = ((Date) value).toLocalDate();
                }

                row.put(metadata.getColumnLabel(i), value);
            }

            rows.add(row);
        }

        return rows;
    }

    //Dispense a prescription
    public boolean dispensePrescription(int prescription_id, int pharmacist_id)
            throws SQLException {

        String sql = "UPDATE Prescription " +
                     "SET status = 'Dispensed', pharmacist_id = ? " +
                     "WHERE prescription_id = ? AND status = 'Pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pharmacist_id);
            stmt.setInt(2, prescription_id);

            return stmt.executeUpdate() > 0;
        }
    }

    //Cancel a pending prescription
    public boolean cancelPrescription(int prescription_id)
            throws SQLException {

        String sql = "UPDATE Prescription SET status = 'Cancelled' " +
                     "WHERE prescription_id = ? AND status = 'Pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, prescription_id);

            return stmt.executeUpdate() > 0;
        }
    }

    //Add a prescription and its bill together
    public boolean addPrescriptionWithBill(Prescription prescription)
            throws SQLException {

        String prescriptionSql = "INSERT INTO Prescription " +
                     "(patient_id, doctor_id, medication_id, " +
                     "pharmacist_id, `date`, status) " +
                     "VALUES (?, ?, ?, NULL, ?, 'Pending')";

        String billSql = "INSERT INTO Bill " +
                     "(patient_id, prescription_id, amount, `date`, " +
                     "description, payment_status) " +
                     "SELECT pr.patient_id, pr.prescription_id, m.price, pr.`date`, " +
                     "CONCAT(m.name, ' prescription'), 'Unpaid' " +
                     "FROM Prescription pr " +
                     "JOIN Medication m ON pr.medication_id = m.medication_id " +
                     "WHERE pr.prescription_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(
                         prescriptionSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement billStmt = conn.prepareStatement(billSql)) {

                stmt.setInt(1, prescription.getPatient_id());
                stmt.setInt(2, prescription.getDoctor_id());
                stmt.setInt(3, prescription.getMedication_id());
                stmt.setDate(4, Date.valueOf(prescription.getDate()));
                stmt.executeUpdate();

                int newId;
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    newId = keys.getInt(1);
                }

                billStmt.setInt(1, newId);
                billStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    //Cancel a pending prescription and remove its unpaid bill
    public boolean cancelPrescriptionAndBill(int prescription_id)
            throws SQLException {

        String cancelSql = "UPDATE Prescription SET status = 'Cancelled' " +
                     "WHERE prescription_id = ? AND status = 'Pending'";

        String billSql = "DELETE FROM Bill " +
                     "WHERE prescription_id = ? AND payment_status = 'Unpaid'";

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(cancelSql);
                 PreparedStatement billStmt = conn.prepareStatement(billSql)) {

                stmt.setInt(1, prescription_id);

                if (stmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }

                billStmt.setInt(1, prescription_id);
                billStmt.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}

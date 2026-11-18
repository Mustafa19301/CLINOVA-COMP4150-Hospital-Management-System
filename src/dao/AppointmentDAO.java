package dao;

import database.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDAO {

    public List<Map<String, Object>> getAppointmentsForStaff(
            int staffId, String role) throws SQLException {

        return retrieveAppointments(staffId, role, null);
    }

    public List<Map<String, Object>> searchAppointmentsForStaff(
            int staffId, String role, String searchTerm)
            throws SQLException {

        return retrieveAppointments(staffId, role, searchTerm);
    }

    private List<Map<String, Object>> retrieveAppointments(
            int staffId, String role, String searchTerm)
            throws SQLException {

        String staffColumn = getStaffColumn(role);

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "a.appointment_id, " +
                        "a.patient_id, " +
                        "p.name AS patient_name, " +
                        "a.doctor_id, " +
                        "ds.name AS doctor_name, " +
                        "a.nurse_id, " +
                        "ns.name AS nurse_name, " +
                        "a.`date` AS appointment_date, " +
                        "a.`time` AS appointment_time, " +
                        "a.reason, " +
                        "a.status " +
                        "FROM Appointment a " +
                        "JOIN Patient p ON a.patient_id = p.patient_id " +
                        "JOIN Staff ds ON a.doctor_id = ds.staff_id " +
                        "JOIN Staff ns ON a.nurse_id = ns.staff_id " +
                        "WHERE a." + staffColumn + " = ? "
        );

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND p.name LIKE ? ");
        }

        sql.append(
                "ORDER BY a.`date` ASC, a.`time` ASC"
        );

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            statement.setInt(1, staffId);

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                statement.setString(2, "%" + searchTerm.trim() + "%");
            }

            try (ResultSet result = statement.executeQuery()) {
                return readRows(result);
            }
        }
    }

    public List<Map<String, Object>> getAllDoctors()
            throws SQLException {

        String sql =
                "SELECT d.staff_id, s.name, d.specialization " +
                        "FROM Doctor d " +
                        "JOIN Staff s ON d.staff_id = s.staff_id " +
                        "ORDER BY s.name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            return readRows(result);
        }
    }

    public List<Map<String, Object>> getAllNurses()
            throws SQLException {

        String sql =
                "SELECT n.staff_id, s.name " +
                        "FROM Nurse n " +
                        "JOIN Staff s ON n.staff_id = s.staff_id " +
                        "ORDER BY s.name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            return readRows(result);
        }
    }

    public String bookAppointment(
            int patientId,
            int staffId,
            String role,
            int selectedDoctorId,
            int selectedNurseId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason) throws SQLException {

        String normalizedRole = normalizeRole(role);

        int doctorId = selectedDoctorId;
        int nurseId = selectedNurseId;

        if ("Doctor".equals(normalizedRole)) {
            doctorId = staffId;
        } else if ("Nurse".equals(normalizedRole)) {
            nurseId = staffId;
        } else {
            throw new IllegalArgumentException(
                    "Only doctors and nurses can book appointments."
            );
        }

        if (appointmentDate == null || appointmentTime == null) {
            throw new IllegalArgumentException(
                    "Appointment date and time are required."
            );
        }

        String sql = "{CALL BookAppointment(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection connection = DatabaseConnection.getConnection();
             CallableStatement statement =
                     connection.prepareCall(sql)) {

            statement.setInt(1, patientId);
            statement.setInt(2, doctorId);
            statement.setInt(3, nurseId);
            statement.setDate(4, Date.valueOf(appointmentDate));
            statement.setTime(5, Time.valueOf(appointmentTime));
            statement.setString(6, reason);

            statement.registerOutParameter(7, Types.VARCHAR);

            statement.execute();

            return statement.getString(7);
        }
    }

    public boolean updateAppointment(
            int appointmentId,
            int staffId,
            String role,
            int patientId,
            int selectedDoctorId,
            int selectedNurseId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            String reason) throws SQLException {

        String normalizedRole = normalizeRole(role);

        int doctorId = selectedDoctorId;
        int nurseId = selectedNurseId;

        String sql;

        if ("Doctor".equals(normalizedRole)) {

            doctorId = staffId;

            sql =
                    "UPDATE Appointment SET " +
                            "patient_id = ?, " +
                            "doctor_id = ?, " +
                            "nurse_id = ?, " +
                            "`date` = ?, " +
                            "`time` = ?, " +
                            "reason = ? " +
                            "WHERE appointment_id = ? " +
                            "AND doctor_id = ?";

        } else if ("Nurse".equals(normalizedRole)) {

            nurseId = staffId;

            sql =
                    "UPDATE Appointment SET " +
                            "patient_id = ?, " +
                            "doctor_id = ?, " +
                            "nurse_id = ?, " +
                            "`date` = ?, " +
                            "`time` = ?, " +
                            "reason = ? " +
                            "WHERE appointment_id = ? " +
                            "AND nurse_id = ?";

        } else {
            throw new IllegalArgumentException(
                    "Only doctors and nurses can update appointments."
            );
        }

        if (appointmentDate == null || appointmentTime == null) {
            throw new IllegalArgumentException(
                    "Appointment date and time are required."
            );
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);
            statement.setInt(2, doctorId);
            statement.setInt(3, nurseId);
            statement.setDate(4, Date.valueOf(appointmentDate));
            statement.setTime(5, Time.valueOf(appointmentTime));
            statement.setString(6, reason);
            statement.setInt(7, appointmentId);
            statement.setInt(8, staffId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean cancelAppointment(
            int appointmentId,
            int staffId,
            String role) throws SQLException {

        return updateAppointmentStatus(
                appointmentId,
                staffId,
                role,
                "Cancelled"
        );
    }

    public boolean updateAppointmentStatus(
            int appointmentId,
            int staffId,
            String role,
            String status) throws SQLException {

        String normalizedRole = normalizeRole(role);

        if (!status.equals("Scheduled")
                && !status.equals("Completed")
                && !status.equals("Cancelled")) {

            throw new IllegalArgumentException(
                    "Invalid appointment status."
            );
        }

        String staffColumn = getStaffColumn(normalizedRole);

        String sql =
                "UPDATE Appointment " +
                        "SET status = ? " +
                        "WHERE appointment_id = ? " +
                        "AND " + staffColumn + " = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, appointmentId);
            statement.setInt(3, staffId);

            return statement.executeUpdate() > 0;
        }
    }

    private String normalizeRole(String role) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "Staff role cannot be null."
            );
        }

        if (role.equalsIgnoreCase("Doctor")) {
            return "Doctor";
        }

        if (role.equalsIgnoreCase("Nurse")) {
            return "Nurse";
        }

        throw new IllegalArgumentException(
                "Unsupported staff role: " + role
        );
    }

    private String getStaffColumn(String role) {

        String normalizedRole = normalizeRole(role);

        if ("Doctor".equals(normalizedRole)) {
            return "doctor_id";
        }

        return "nurse_id";
    }

    private List<Map<String, Object>> readRows(
            ResultSet result) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        ResultSetMetaData metadata = result.getMetaData();
        int columnCount = metadata.getColumnCount();

        while (result.next()) {

            Map<String, Object> row = new LinkedHashMap<>();

            for (int i = 1; i <= columnCount; i++) {

                String columnName = metadata.getColumnLabel(i);
                Object value = result.getObject(i);

                if (value instanceof Date) {
                    value = ((Date) value).toLocalDate();

                } else if (value instanceof Time) {
                    value = ((Time) value).toLocalTime();
                }

                row.put(columnName, value);
            }

            rows.add(row);
        }

        return rows;
    }
}
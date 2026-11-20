# CLINOVA - COMP4150-Hospital-Management-System
Hospital Management System developed for COMP4150 Fall 2026

Group Members:
- Mustafa Ahmed
- Murtaza Ahmed
- Xinglon Yan
- Michael Powell

## Project Overview
CLINOVA is an Hospital Management System application designed to support essential hospital operations through an integrated graphical user interface and relational database.
The system uses Java Swing for its desktop interface and MySQL for persistent data storage. It manages patients, medical staff, appointments, medical records, prescriptions, medications and billing.
CLINOVA provides role based access for different staff members and integrates its graphical interface with database operations to support day to day hospital management tasks.

## Technologies
- Java 17
- Java Swing
- JDBC
- MySQL
- Git & GitHub

## Database Features
- Staff
- Doctor
- Nurse
- PharmacyStaff
- Patient
- Appointment
- MedicalRecord
- Medication
- Prescription
- Bill
- BillAudit
- PrescriptionAudit

### Functionality
#### Patient Management
- Add, update, delete and retrieve patient information
- Search patients by ID, name and phone number
- Retrieve patient medical hisotires

#### Medical Records
- Create, update, delete and retrieve medical records
- Associate medical records with patient and doctors
- Retrieve medical records by doctor

#### Appointments
- Manage staff and appointment information
- Validate doctor, nurse and patient scheduling conflicts
- Support appointment booking through stored procedures

#### Prescriptions and Billing
- Manage prescription and billing information
- Track changes using audit tables and triggers
- Support queries for outstanding bills and prescription information

### Database integrity
- Preventing blank patient names
- Preventing future patient date of birth
- Preventing future medical record dates
- Preventing invalid appointment scheduling conflicts

## Graphical User Interface (GUI)
CLINOVA includes a fully functional Java Swing desktop application integrated with the MySQL database.
The interface provides a centralized dashboard and dedicated management panels for the system's major hospital operations.

### Dashboard
- Centralized dashboard for navigating the application's modules
- Displays relevant hospital management information
- Provides an organized interface for accessing available functionality

### User Authentication and Role Access
- Supports separate staff login credentials
- Provides role access for Doctor, Nurse, and Pharmacy Staff accounts
- Restricts or tailors available functionality according to the authenticated staff member's role
- Provides role access to relevant management modules

### Patient Directory
- View and search registered patients
- Add new patient records
- Update existing patient information
- Delete patient records

### Medical Records Management
- Create and manage patient medical records
- View existing medical history
- Update and delete medical records
- Associate records with the relevant patient and doctor

### Appointment Scheduler
- Calendar-based appointment scheduling interface
- View appointments organized by date
- Book and edit appointments
- Mark appointments as completed or cancel scheduled appointments
- View appointment details and status

### Prescription Management
- Create and manage patient prescriptions
- Track prescription statuses
- Support prescription dispensing workflows
- Integrate prescription creation with automatic bill generation

### Billing Management
- View existing patient bills
- Search bills using patient IDs
- Update payment statuses
- Delete existing bill records
- Display patient names alongside their billing information

## Database Integration
- GUI operations are connected to the MySQL database through JDBC and DAO classes
- Changes made through the interface are persisted in the database
- Database constraints, stored procedures, and triggers support data integrity and business rules

## Running Application
- Install Java 17
- Install MySQL Server
- Import the provided SQL scripts to initialize the database
- Configure the database connection credentials in the application
- Ensure the MySQL Connector/J library is available to the project
- Open the project in a compatible Java IDE
- Run main.HospitalManagementApp to launch CLINOVA
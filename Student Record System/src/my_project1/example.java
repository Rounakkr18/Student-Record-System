package my_project1;

import java.sql.*;
import java.util.Scanner;
import io.github.cdimascio.dotenv.Dotenv;

public class Example {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USERNAME = dotenv.get("DB_USERNAME");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");
}

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (connection != null) {
                System.out.println("Connected to the database!");

                // Create tables if they don't exist
                createTables(connection);

                // Run the program until the user chooses to exit
                boolean exitProgram = false;
                Scanner scanner = new Scanner(System.in);
                while (!exitProgram) {
                    // Display menu options
                    System.out.println("\nMenu:");
                    System.out.println("1. Student Registration");
                    System.out.println("2. Course Registration");
                    System.out.println("3. Add Course");
                    System.out.println("4. Show Courses");
                    System.out.println("5. Show Details");
                    System.out.println("6. Exit");
                    System.out.print("Enter your choice: ");

                    // Read user choice
                    int choice = scanner.nextInt();

                    switch (choice) {
                        case 1:
                            registerStudent(connection);
                            break;
                        case 2:
                            registerCourse(connection);
                            break;
                        case 3:
                            addCourse(connection);
                            break;
                        case 4:
                            showCourses(connection);
                            break;
                        case 5:
                            showDetails(connection);
                            break;
                        case 6:
                            exitProgram = true;
                            System.out.println("Exiting program...");
                            break;
                        default:
                            System.out.println("Invalid choice!");
                    }
                }

                scanner.close();
                connection.close();
            } else {
                System.out.println("Failed to connect to the database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        // Create 'student' table
        String createStudentTable = "CREATE TABLE IF NOT EXISTS student (" +
                                    "student_id INT AUTO_INCREMENT PRIMARY KEY," +
                                    "name VARCHAR(255)," +
                                    "dob DATE," +
                                    "gender VARCHAR(10)," +
                                    "phone VARCHAR(20)," +
                                    "email VARCHAR(100)," +
                                    "father_name VARCHAR(255)," +
                                    "address VARCHAR(255)" +
                                    ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createStudentTable);
        }

        // Create 'course' table
        String createCourseTable = "CREATE TABLE IF NOT EXISTS course (" +
                                   "course_id INT AUTO_INCREMENT PRIMARY KEY," +
                                   "student_id INT," +
                                   "course_name VARCHAR(255)," +
                                   "FOREIGN KEY (student_id) REFERENCES student(student_id)" +
                                   ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createCourseTable);
        }

        // Create 'available_course' table
        String createAvailableCourseTable = "CREATE TABLE IF NOT EXISTS available_course (" +
                                            "course_id INT AUTO_INCREMENT PRIMARY KEY," +
                                            "course_name VARCHAR(255)" +
                                            ")";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createAvailableCourseTable);
        }
    }

    private static void registerStudent(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nStudent Registration:");
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        System.out.print("Enter date of birth (YYYY-MM-DD): ");
        String dob = scanner.next();
        System.out.print("Enter gender: ");
        String gender = scanner.next();
        System.out.print("Enter phone number: ");
        String phone = scanner.next();
        System.out.print("Enter email: ");
        String email = scanner.next();
        System.out.print("Enter father's name: ");
        String fatherName = scanner.next();
        System.out.print("Enter address: ");
        scanner.nextLine(); // Consume newline character
        String address = scanner.nextLine();

        String insertQuery = "INSERT INTO student (name, dob, gender, phone, email, father_name, address) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, dob);
            preparedStatement.setString(3, gender);
            preparedStatement.setString(4, phone);
            preparedStatement.setString(5, email);
            preparedStatement.setString(6, fatherName);
            preparedStatement.setString(7, address);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Student registered successfully!");
            } else {
                System.out.println("Failed to register student!");
            }
        }
    }

    private static void registerCourse(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nCourse Registration:");

        // Prompt user to enter student ID
        System.out.print("Enter student ID: ");
        int studentId = scanner.nextInt();

        // Check if student ID exists in the student table
        boolean studentExists = checkStudentExists(connection, studentId);
        if (!studentExists) {
            System.out.println("Student with this ID is not registered.");
            return;
        }

        // Display list of available courses
        displayAvailableCourses(connection);

        // Prompt user to enter course ID
        System.out.print("Enter course ID: ");
        int courseId = scanner.nextInt();

        // Check if the entered course ID exists
        boolean courseExists = checkCourseExists(connection, courseId);
        if (!courseExists) {
            System.out.println("Course with this ID does not exist.");
            return;
        }

        // Get the course name
        String courseName = getCourseName(connection, courseId);

        // Insert course registration details into the database
        String insertQuery = "INSERT INTO course (student_id, course_id, course_name) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, courseId);
            preparedStatement.setString(3, courseName);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Course registered successfully for student ID: " + studentId);
            } else {
                System.out.println("Failed to register course!");
            }
        }
    }

    private static String getCourseName(Connection connection, int courseId) throws SQLException {
        String query = "SELECT course_name FROM available_course WHERE course_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, courseId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("course_name");
                }
            }
        }
        return null;
    }



    private static void addCourse(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nAdd Course:");

        System.out.print("Enter course name: ");
        String courseName = scanner.nextLine();

        String insertQuery = "INSERT INTO available_course (course_name) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, courseName);
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Course added successfully!");
            } else {
                System.out.println("Failed to add course!");
            }
        }
    }
    
    private static boolean checkStudentExists(Connection connection, int studentId) throws SQLException {
        String query = "SELECT * FROM student WHERE student_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If any row is returned, student exists
            }
        }
    }

    private static boolean checkCourseExists(Connection connection, int courseId) throws SQLException {
        String query = "SELECT * FROM available_course WHERE course_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, courseId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // If any row is returned, course exists
            }
        }
    }

    private static void displayAvailableCourses(Connection connection) throws SQLException {
        System.out.println("Available Courses:");
        String query = "SELECT course_id, course_name FROM available_course";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                System.out.println(courseId + ". " + courseName);
            }
        }
    }

    private static void showCourses(Connection connection) throws SQLException {
        System.out.println("\nAvailable Courses:");
        String query = "SELECT course_id, course_name FROM available_course";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                String courseName = resultSet.getString("course_name");
                System.out.println(courseId + ". " + courseName);
            }
        }
    }

    private static void showDetails(Connection connection) throws SQLException {
        String query = "SELECT s.student_id, s.name, s.dob, s.gender, s.phone, s.email, s.father_name, s.address, c.course_name " +
                       "FROM student s LEFT JOIN course c ON s.student_id = c.student_id";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            System.out.println("\nStudent Details:");
            System.out.println("ID\tName\tDOB\t\tGender\tPhone\t\tEmail\t\tFather's Name\tAddress\t\tCourse");
            while (resultSet.next()) {
                int studentId = resultSet.getInt("student_id");
                String name = resultSet.getString("name");
                String dob = resultSet.getString("dob");
                String gender = resultSet.getString("gender");
                String phone = resultSet.getString("phone");
                String email = resultSet.getString("email");
                String fatherName = resultSet.getString("father_name");
                String address = resultSet.getString("address");
                String courseName = resultSet.getString("course_name");
                System.out.println(studentId + "\t" + name + "\t" + dob + "\t" + gender + "\t" + phone + "\t" +
                                   email + "\t" + fatherName + "\t\t"+address + "\t\t" + courseName);
            }
        }
    }
}


import java.sql.*;

public class SQLiteHandler {

    private static final String DB_URL = "jdbc:sqlite:tasks.db";

    // Initialize the database and create tables if not present
    public static void initDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "task_name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "completed BOOLEAN NOT NULL DEFAULT 0" +
                    ")";
            stmt.execute(createTableSQL);
        }
    }

    // Add a new task to the database
    public static void addTask(String taskName, String category, boolean isCompleted) throws SQLException {
        String insertSQL = "INSERT INTO tasks (task_name, category, completed) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, taskName);
            pstmt.setString(2, category);
            pstmt.setBoolean(3, false); // Default as incomplete
            pstmt.executeUpdate();
        }
    }

    // Mark a task as completed in the database
    public static void markTaskAsCompleted(int taskId) throws SQLException {
        String updateSQL = "UPDATE tasks SET completed = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setBoolean(1, true);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        }
    }

    // Retrieve all tasks from the database
    public static ResultSet getTasks() throws SQLException {
        String selectSQL = "SELECT * FROM tasks";
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(selectSQL); // ResultSet will be iterated in GUI
    }

    // Retrieve tasks filtered by category
    public static ResultSet getTasksByCategory(String category) throws SQLException {
        String selectSQL = "SELECT * FROM tasks WHERE category = ?";
        Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(selectSQL);
        pstmt.setString(1, category);
        return pstmt.executeQuery(); // ResultSet will be iterated in GUI
    }

    // Delete task by ID (not currently used in TaskGUI but useful for future)
    public static void deleteTask(int taskId) throws SQLException {
        String deleteSQL = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
        }
    }

    // Check if a task with a given ID exists
    public static boolean taskExists(int taskId) throws SQLException {
        String query = "SELECT COUNT(*) FROM tasks WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    // Update an existing task (used for the "Replace" option)
    public static void updateTask(int taskId, String taskName, String category, boolean isCompleted) throws SQLException {
        String updateSQL = "UPDATE tasks SET task_name = ?, category = ?, completed = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setString(1, taskName);
            pstmt.setString(2, category);
            pstmt.setBoolean(3, isCompleted);
            pstmt.setInt(4, taskId);
            pstmt.executeUpdate();
        }
    }

}
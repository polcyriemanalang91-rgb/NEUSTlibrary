package neustlibrarysystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String URL =
        "jdbc:sqlserver://localhost\\SQLEXPRESS"
        + ";databaseName=NEUSTLibraryDB"
        + ";integratedSecurity=false"
        + ";encrypt=false"
        + ";trustServerCertificate=true";

    // ✅ FIXED: password updated to match ALTER LOGIN command
    private static final String USER     = "sa";
    private static final String PASSWORD = "sa123";

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                LOGGER.info("Database connection established.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect: " + e.getMessage(), e);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing connection.", e);
            }
        }
    }

    public static boolean testConnection() {
        return getConnection() != null;
    }
}
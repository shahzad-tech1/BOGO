package org.BOGO.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration for MS SQL Server connection.
 */
public class DatabaseConfig {
    
    private static final String URL = "jdbc:sqlserver://DESKTOP-M0688UR\\SQLEXPRESS;databaseName=bogo;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "BOGO";
    private static final String PASSWORD = "ABD-3740-2006";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

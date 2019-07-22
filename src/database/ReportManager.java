package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ReportManager {

    public static boolean totalNumberOfBookings(Connection conn, Scanner reader) {
        try {
            Statement st = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }
}

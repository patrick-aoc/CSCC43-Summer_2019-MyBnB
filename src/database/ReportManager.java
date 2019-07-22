package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ReportManager {

    public static boolean totalNumberOfBookings(Connection conn, Scanner reader) {
        try {
            // Ask for dates
            System.out.print("Enter a starting date (YYYY-MM-DD): ");
            String startDate = reader.nextLine();
            System.out.print("Enter an ending date (YYYY-MM-DD): ");
            String endDate = reader.nextLine();

            if(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // Get all the dates that fall within the given range
                String city = "SELECT COUNT(Listings.listing_id), Listings.city" +
                        " FROM Listings NATURAL JOIN Calendar " +
                        "WHERE (Calendar.calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "GROUP BY Listings.city";
                Statement st = conn.createStatement();
                ResultSet res = st.executeQuery(city);

                String postal = "SELECT COUNT(Listings.listing_id), Listings.postal_code" +
                        " FROM Listings NATURAL JOIN Calendar " +
                        "WHERE (Calendar.calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "GROUP BY Listings.postal_code";
                ResultSet res2 = st.executeQuery(postal);

                System.out.println("------------BOOKING COUNTS BY CITY------------");
                while(res.next()) {
                    System.out.println("City: " + res.getString(2));
                    System.out.println("Count: " + res.getInt(1));
                    System.out.println("-------------------------------------------");
                }

                System.out.println("-------------BOOKING COUNTS BY POSTAL CODE-------------");
                while(res2.next()) {
                    System.out.println("Postal Code: " + res2.getString(2));
                    System.out.println("Count: " + res2.getInt(1));
                    System.out.println("---------------------------------------------------");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean totalNumberOfListings(Connection conn, Scanner reader) {
        try {
            String countryQ = "SELECT COUNT(listing_id), country FROM Listings GROUP BY country";
            String countryCityQ = "SELECT COUNT(listing_id), country, city FROM Listings GROUP BY country, city";
            String countryCityPostalQ = "SELECT COUNT(listing_id), country, city, postal_code FROM Listings GROUP BY country, city, postal_code";
            Statement st = conn.createStatement();

            ResultSet r1 = st.executeQuery(countryQ);
            ResultSet r2 = st.executeQuery(countryCityQ);
            ResultSet r3 = st.executeQuery(countryCityPostalQ);

            System.out.println("-----LISTINGS BY COUNTRY-----");
            while(r1.next()) {
                System.out.println("Country: " + r1.getString(2));
                System.out.println("Count: " + r1.getInt(1));
                System.out.println("--------------------------");
            }

            System.out.println("-----LISTINGS BY COUNTRY/CITY-----");
            while(r2.next()) {
                System.out.println("Country: " + r2.getString(2));
                System.out.println("City: " + r2.getString(3));
                System.out.println("Count: " + r2.getInt(1));
                System.out.println("--------------------------");
            }

            System.out.println("-----LISTINGS BY COUNTRY/CITY/POSTAL CODE-----");
            while(r3.next()) {
                System.out.println("Country: " + r3.getString(2));
                System.out.println("City: " + r3.getString(3));
                System.out.println("Postal Code: " + r3.getString(4));
                System.out.println("Count: " + r3.getInt(1));
                System.out.println("--------------------------");
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean topHosts(Connection conn, Scanner reader) {
        try {
            Statement st = conn.createStatement();
            String topHosts = "";


            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

}

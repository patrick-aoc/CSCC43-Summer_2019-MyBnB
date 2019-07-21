package listings;

import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SearchManager {

    private static final String latRegex = "";

    public static boolean searchByLatLong(Connection conn, Scanner reader) {
        try {
            BigDecimal latitude;
            BigDecimal longitude;
            BigDecimal distance = BigDecimal.ZERO;
            BigDecimal latMin = new BigDecimal(-90);
            BigDecimal latMax = new BigDecimal(90);
            BigDecimal longMin = new BigDecimal(-180);
            BigDecimal longMax = new BigDecimal(180);

            // Ask for a latitude input and validate it
            System.out.print("Enter a latitude: ");
            String latInput = reader.nextLine();
            if (latInput.contains("[a-zA-Z]+")) {
                System.err.println("[ERROR] : Invalid latitude specified");
                return false;
            } else {
                latitude = new BigDecimal(Double.parseDouble(latInput));
                if(!((latitude.compareTo(latMin) == 1) && (latitude.compareTo(latMax) == -1))) {
                    System.err.println("[ERROR] : Invalid latitude specified");
                    return false;
                }
            }

            // Ask for a longitude input and validate it
            System.out.print("Enter a longitude: ");
            String longInput = reader.nextLine();
            if (longInput.contains("[a-zA-Z]+")) {
                System.err.println("[ERROR] : Invalid longitude specified");
                return false;
            } else {
                longitude = new BigDecimal(Double.parseDouble(longInput));
                if(!((longitude.compareTo(longMin) == 1) && (longitude.compareTo(longMax) == -1))) {
                    System.err.println("[ERROR] : Invalid latitude specified");
                    return false;
                }
            }

            // Ask for a distance input and validate it
            System.out.print("Enter a distance (in kilometres) or leave this field blank - default distance is 5 km: ");
            String distanceInput = reader.nextLine();
            if (!distanceInput.isEmpty()) {
                if (distanceInput.contains("[a-zA-Z]+")) {
                    System.err.println("[ERROR] : Invalid distance specified");
                    return false;
                } else {
                    distance = new BigDecimal(Double.parseDouble(distanceInput));
                    if (distance.compareTo(BigDecimal.ZERO) == -1) {
                        System.err.println("[ERROR] : Invalid distance specified");
                        return false;
                    }
                }
            }

            BigDecimal d = distanceInput.isEmpty() ? new BigDecimal(5) : distance;
            String getListings = "SELECT * FROM Listings";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListings);
            double earthRad = 6371.01;

            String bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                    "Listings.country, Listings.postal_code " +
                    "FROM Listings, Calendar " +
                    "WHERE Listings.listing_id = Calendar.listing_id AND (Listings.listing_id = ";
            while(res.next()) {
                // Get the latitude/longitude of the listing and convert them to radians
                double dLat = Math.toRadians(res.getBigDecimal(4).doubleValue() - latitude.doubleValue());
                double dLong = Math.toRadians(res.getBigDecimal(5).doubleValue() - longitude.doubleValue());
                double dist = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(latitude.doubleValue())) *
                            Math.cos(Math.toRadians(res.getBigDecimal(4).doubleValue()))
                        * Math.sin(dLong / 2) * Math.sin(dLong / 2);
                double distA = 2 * Math.atan2(Math.sqrt(dist), Math.sqrt(1 - dist));
                double distB = earthRad * distA;

                BigDecimal eD = new BigDecimal(distB);
                // If the distance between the listing's lat/long is within the specified distance, add it into our query
                if(eD.compareTo(d) == 0 || eD.compareTo(d) == -1) {
                    bigJoin = bigJoin + res.getInt(3) + " OR Listings.listing_id = ";
                }
            }
            bigJoin = bigJoin.substring(0, bigJoin.length() - 26);

            System.out.print("Sort the listings by ascending/descending order in price? (ASC or DESC)");
            String choice = reader.nextLine();

            if (choice.equalsIgnoreCase("asc")) {
                res = st.executeQuery(bigJoin + ") ORDER BY Calendar.price ASC");
            } else if (choice.equalsIgnoreCase("desc")) {
                res = st.executeQuery(bigJoin + ") ORDER BY Calendar.price DESC");
            } else {
                res = st.executeQuery(bigJoin + ")");
            }

            System.out.println("-----LISTINGS/BOOKING DATES WITHIN YOUR RANGE-----");
            while(res.next()) {
                System.out.println("Listing ID: " + res.getInt(1));
                System.out.println("Listing Price: " + res.getDouble(2));
                System.out.println("Listing Country: " + res.getString(5));
                System.out.println("Listing City: " + res.getString(4));
                System.out.println("Listing Postal Code: " + res.getString(6));
                System.out.println("Listing Date: " + res.getString(3));
                System.out.println("-----------------------------------------------");
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
     }

    public static boolean searchByPostalCode(Connection conn, Scanner reader) {
//        try {
//
//        } catch (SQLException e) {
//            System.err.println("[ERROR] : There was a problem with your input");
//            e.printStackTrace();
//        }
        return false;
    }
}

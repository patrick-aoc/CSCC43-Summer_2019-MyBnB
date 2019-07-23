package listings;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SearchManager {

    public static boolean searchByLatLong(Connection conn, Scanner reader) {
        try {
            BigDecimal latitude;
            BigDecimal longitude;
            BigDecimal distance = BigDecimal.ZERO;
            BigDecimal latMin = new BigDecimal(-90);
            BigDecimal latMax = new BigDecimal(90);
            BigDecimal longMin = new BigDecimal(-180);
            BigDecimal longMax = new BigDecimal(180);

            System.out.print("Enter a starting date (YYYY-MM-DD) or leave this field blank: ");
            String startDate = reader.nextLine();
            String endDate = "";

            if(!startDate.isEmpty()) {
                System.out.print("Enter an ending date (YYYY-MM-DD): ");
                endDate = reader.nextLine();
            }

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

            String bigJoin = "";

            if (!(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}"))){
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (Listings.listing_id = ";
            } else {
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "AND (Listings.listing_id = ";
            }

            int queryAdded = 0;
            while(res.next()) {
                // Get the latitude/longitude of the listing and convert them to radians

                // LINES 85 TO 92 involve the use of the Haversine method of computing distances between
                // 2 latitude/longitude points
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
                    queryAdded++;
                }
            }
            if (queryAdded != 0) {
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
                    System.out.println("Listing Price: $" + res.getDouble(2));
                    System.out.println("Listing Country: " + res.getString(5));
                    System.out.println("Listing City: " + res.getString(4));
                    System.out.println("Listing Postal Code: " + res.getString(6));
                    System.out.println("Listing Date: " + res.getString(3));
                    System.out.println("-----------------------------------------------");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
     }

    public static boolean searchByPostalCode(Connection conn, Scanner reader) {
        try {
            System.out.print("Enter a starting date (YYYY-MM-DD) or leave this field blank: ");
            String startDate = reader.nextLine();
            String endDate = "";

            if(!startDate.isEmpty()) {
                System.out.print("Enter an ending date (YYYY-MM-DD): ");
                endDate = reader.nextLine();
            }

            // Ask for a postal code
            System.out.print("Enter a postal code: ");
            String postalCode = reader.nextLine();

            String getListings = "SELECT * FROM Listings";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListings);

            String bigJoin = "";

            if (!(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}"))){
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (Listings.listing_id = ";
            } else {
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "AND (Listings.listing_id = ";
            }

            int queryAdded = 0;
            while(res.next()) {
                String currPostalCode = res.getString(6);

                // If the first 3 characters are the same, then add it to the results query
                if(postalCode.substring(0, 3).equalsIgnoreCase(currPostalCode.substring(0, 3))) {
                    bigJoin = bigJoin + res.getInt(3) + " OR Listings.listing_id = ";
                    queryAdded++;
                }
            }
            bigJoin = bigJoin.substring(0, bigJoin.length() - 26);
            if (queryAdded != 0) {
                res = st.executeQuery(bigJoin + ")");

                System.out.println("-----LISTINGS/BOOKING DATES ADJACENT TO YOUR POSTAL CODE-----");
                while (res.next()) {
                    System.out.println("Listing ID: " + res.getInt(1));
                    System.out.println("Listing Price: $" + res.getDouble(2));
                    System.out.println("Listing Country: " + res.getString(5));
                    System.out.println("Listing City: " + res.getString(4));
                    System.out.println("Listing Postal Code: " + res.getString(6));
                    System.out.println("Listing Date: " + res.getString(3));
                    System.out.println("-----------------------------------------------");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean searchByAddress(Connection conn, Scanner reader) {
        try {
            System.out.print("Enter a starting date (YYYY-MM-DD) or leave this field blank: ");
            String startDate = reader.nextLine();
            String endDate = "";

            if(!startDate.isEmpty()) {
                System.out.print("Enter an ending date (YYYY-MM-DD): ");
                endDate = reader.nextLine();
            }

            // Ask for a postal code
            System.out.print("Enter a postal code: ");
            String postalCode = reader.nextLine();

            // Ask for a city
            System.out.print("Enter a city: ");
            String city = reader.nextLine();

            // Ask for a country
            System.out.print("Enter a country: ");
            String country = reader.nextLine();

            String getListings = "SELECT * FROM Listings";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListings);

            String bigJoin = "";
            if (!(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}"))){
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (Listings.listing_id = ";
            } else {
                bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "AND (Listings.listing_id = ";
            }

            int queryAdded = 0;
            while(res.next()) {
                String currPostalCode = res.getString(6);
                String currCity = res.getString(7);
                String currCountry = res.getString(8);

                // If the first 3 characters are the same, then add it to the results query
                if(currPostalCode.equalsIgnoreCase(postalCode) && currCity.equalsIgnoreCase(city) &&
                        currCountry.equalsIgnoreCase(country)) {
                    bigJoin = bigJoin + res.getInt(3) + " OR Listings.listing_id = ";
                    queryAdded++;
                }
            }
            bigJoin = bigJoin.substring(0, bigJoin.length() - 26);
            if (queryAdded != 0) {
                res = st.executeQuery(bigJoin + ")");

                System.out.println("-----LISTINGS/BOOKING DATES THAT MATCH YOUR ADDRESS-----");
                while (res.next()) {
                    System.out.println("Listing ID: " + res.getInt(1));
                    System.out.println("Listing Price: $" + res.getDouble(2));
                    System.out.println("Listing Country: " + res.getString(5));
                    System.out.println("Listing City: " + res.getString(4));
                    System.out.println("Listing Postal Code: " + res.getString(6));
                    System.out.println("Listing Date: " + res.getString(3));
                    System.out.println("-----------------------------------------------");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean searchByDateRange(Connection conn, Scanner reader) {
        try {
            System.out.print("Enter a starting date (YYYY-MM-DD): ");
            String startDate = reader.nextLine();
            System.out.print("Enter an ending date (YYYY-MM-DD): ");
            String endDate = reader.nextLine();

            if (startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {

                String getListings = "SELECT * FROM Calendar WHERE (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') ";
                Statement st = conn.createStatement();
                ResultSet res = st.executeQuery(getListings);

                String bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') ";
                    res = st.executeQuery(bigJoin);

                    System.out.println("-----LISTINGS/BOOKING DATES IN YOUR GIVEN RANGE-----");
                    while (res.next()) {
                        System.out.println("Listing ID: " + res.getInt(1));
                        System.out.println("Listing Price: $" + res.getDouble(2));
                        System.out.println("Listing Country: " + res.getString(5));
                        System.out.println("Listing City: " + res.getString(4));
                        System.out.println("Listing Postal Code: " + res.getString(6));
                        System.out.println("Listing Date: " + res.getString(3));
                        System.out.println("-----------------------------------------------");
                }
                return true;

            } else {
                System.err.println("[ERROR] : Incorrect format given for the dates");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean searchWithFilters(Connection conn, Scanner reader) {
        try {
            // Ask for a postal code
            System.out.print("Enter a postal code: ");
            String postalCode = reader.nextLine();

            // Ask for dates
            System.out.print("Enter a starting date (YYYY-MM-DD): ");
            String startDate = reader.nextLine();
            System.out.print("Enter an ending date (YYYY-MM-DD): ");
            String endDate = reader.nextLine();

            // Ask for prices
            System.out.print("Enter a minimum price: $");
            String minPrice = reader.nextLine();
            System.out.print("Enter a maximum price: $");
            String maxPrice = reader.nextLine();

            // Ask for amenities
            System.out.print("Enter your desired amenities (separate each amenity by ','): ");
            String[] am = reader.nextLine().split(",");
            List<String> amenities = Arrays.asList(am);

            if(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}") &&
            minPrice.matches("^\\d{0,8}(\\.\\d{1,4})?$") && maxPrice.matches("^\\d{0,8}(\\.\\d{1,4})?$")) {
                String bigJoin = "SELECT Listings.listing_id, Calendar.price, Calendar.calendar_date, Listings.city, " +
                        "Listings.country, Listings.postal_code " +
                        "FROM Listings, Calendar " +
                        "WHERE Listings.listing_id = Calendar.listing_id " +
                        "AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "AND (Listings.listing_id = ";

                String getDates = "SELECT * FROM Calendar";
                Statement st = conn.createStatement();
                ResultSet resD = st.executeQuery(getDates);

                String getListings = "SELECT * FROM Listings NATURAL JOIN Amenities";
                ResultSet resL = st.executeQuery(getListings);

                int queryAdded = 0;
                while(resL.next()) {
                    while(resD.next()) {

                        // If the date is within the range, matches the postal code, and is within the price range
                        if(resL.getString(6).equalsIgnoreCase(postalCode) &&
                                resD.getDouble(2) >= Double.parseDouble(minPrice) &&
                                resD.getDouble(2) <= Double.parseDouble(maxPrice) &&
                                amenities.contains(resL.getString(9)) &&
                                resD.getInt(3) == resL.getInt(1)) {
                            bigJoin = bigJoin + resD.getInt(3) + " OR Listings.listing_id = ";
                            queryAdded++;
                        }
                    }
                    resD = st.executeQuery(getDates);
                }
                bigJoin = bigJoin.substring(0, bigJoin.length() - 26);
                if(queryAdded != 0) {
                    ResultSet res = st.executeQuery(bigJoin + ")");
                    System.out.println("-----LISTINGS/BOOKINGS BASED ON FILTERS-----");
                    while(res.next()) {
                        System.out.println("Listing ID: " + res.getInt(1));
                        System.out.println("Listing Price: $" + res.getDouble(2));
                        System.out.println("Listing Country: " + res.getString(5));
                        System.out.println("Listing City: " + res.getString(4));
                        System.out.println("Listing Postal Code: " + res.getString(6));
                        System.out.println("Listing Date: " + res.getString(3));
                        System.out.println("-----------------------------------------------");
                    }
                }
                return true;
            } else {
                System.err.println("[ERROR] : Incorrect format given for the inputs");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] : There was a problem with your input");
            e.printStackTrace();
        }
        return false;
    }
}

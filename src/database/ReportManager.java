package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

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
            String topHosts = "SELECT COUNT(Listings.listing_id), Users.full_name, country " +
                            "FROM Listings, Users " +
                            "WHERE Listings.sin_host = Users.sin " +
                            "GROUP BY country, sin ORDER BY COUNT(Listings.listing_id) DESC";
            ResultSet res = st.executeQuery(topHosts);

            System.out.println("-----TOP HOSTS PER COUNTRY-----");
            String prevCountry = "";
            while(res.next()) {
                String currCountry = res.getString(3);
                if(!prevCountry.equalsIgnoreCase(currCountry)) {
                    System.out.println("------" + currCountry + "------");
                }
                System.out.println("Name: " + res.getString(2));
                System.out.println("Count: " + res.getInt(1));
                System.out.println("---");
                prevCountry = currCountry;
            }

            String topHostsCity = "SELECT COUNT(Listings.listing_id), Users.full_name, city " +
                    "FROM Listings, Users " +
                    "WHERE Listings.sin_host = Users.sin " +
                    "GROUP BY city, sin ORDER BY COUNT(Listings.listing_id) DESC";
            res = st.executeQuery(topHostsCity);
            System.out.println("-----TOP HOSTS PER CITY-----");
            String prevCity = "";
            while(res.next()) {
                String currCity = res.getString(3);
                if(!prevCity.equalsIgnoreCase(currCity)) {
                    System.out.println("------" + currCity + "------");
                }
                System.out.println("Name: " + res.getString(2));
                System.out.println("Count: " + res.getInt(1));
                System.out.println("---");
                prevCity = currCity;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean listingPercentage(Connection conn, Scanner reader) {
        try {
            Statement st = conn.createStatement();
            String country = "SELECT COUNT(listing_id), country FROM Listings GROUP BY country";
            ResultSet resCountry = st.executeQuery(country);

            HashMap<String, Integer> countries = new HashMap<>();
            while(resCountry.next()) {
                countries.put(resCountry.getString(2), resCountry.getInt(1));
            }

            String city = "SELECT COUNT(listing_id), city FROM Listings GROUP BY city";
            ResultSet resCity = st.executeQuery(city);
            HashMap<String, Integer> cities = new HashMap<>();
            while(resCity.next()) {
                cities.put(resCity.getString(2), resCity.getInt(1));
            }

            String topHosts = "SELECT COUNT(Listings.listing_id), Users.full_name, country " +
                    "FROM Listings, Users " +
                    "WHERE Listings.sin_host = Users.sin " +
                    "GROUP BY country, sin ORDER BY COUNT(Listings.listing_id) DESC";
            ResultSet res1 = st.executeQuery(topHosts);

            System.out.println("=====USERS PER COUNTRY THAT GO OVER THE 10%=====");
            while(res1.next()) {
                int listingCount = res1.getInt(1);
                double listingAmt = ((double) listingCount) / countries.get(res1.getString(3));
                if (listingAmt > 0.1) {
                    System.out.println("Name: " + res1.getString(2));
                    System.out.println("Country: " + res1.getString(3));
                    System.out.println("Listing Percentage: " + listingAmt);
                    System.out.println("---");
                }
            }

            String topHostsCity = "SELECT COUNT(Listings.listing_id), Users.full_name, city " +
                    "FROM Listings, Users " +
                    "WHERE Listings.sin_host = Users.sin " +
                    "GROUP BY city, sin ORDER BY COUNT(Listings.listing_id) DESC";
            ResultSet res2 = st.executeQuery(topHostsCity);
            System.out.println("=====USERS PER CITY THAT GO OVER THE 10%=====");
            while(res2.next()) {
                int listingCount = res2.getInt(1);
                double listingAmt = ((double) listingCount) / cities.get(res2.getString(3));
                if (listingAmt > 0.1) {
                    System.out.println("Name: " + res2.getString(2));
                    System.out.println("City: " + res2.getString(3));
                    System.out.println("Listing Percentage: " + listingAmt);
                    System.out.println("---");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean topRenters(Connection conn, Scanner reader) {
        try {
            // Ask for dates
            System.out.print("Enter a starting date (YYYY-MM-DD): ");
            String startDate = reader.nextLine();
            System.out.print("Enter an ending date (YYYY-MM-DD): ");
            String endDate = reader.nextLine();

            if(startDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                Statement st  = conn.createStatement();

                // Get all the dates that fall within the given range
                String timeQ = "SELECT COUNT(listing_id), full_name " +
                        " FROM Calendar, Users " +
                        "WHERE sin_renter = sin AND (calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "GROUP BY sin_renter";
                ResultSet res1 = st.executeQuery(timeQ);

                System.out.println("====TOP RENTERS BETWEEN " + startDate + " AND " + endDate + "=====");
                while(res1.next()) {
                    System.out.println("Name: " + res1.getString(2));
                    System.out.println("Number of Bookings: " + res1.getInt(1));
                    System.out.println("---");
                }

                String timeCityQ = "SELECT COUNT(c.listing_id), l.city, u.full_name " +
                        "FROM Calendar c, Users u, Listings l " +
                        "WHERE c.sin_renter = u.sin AND l.listing_id = c.listing_id AND (c.calendar_date BETWEEN '" + startDate + "' AND '" + endDate + "') " +
                        "GROUP BY l.city, u.full_name";
                ResultSet res2 = st.executeQuery(timeCityQ);

                System.out.println("====TOP RENTERS BETWEEN " + startDate + " AND " + endDate + " BY CITY=====");
                String prevCity = "";
                while(res2.next()) {
                    String currCity = res2.getString(2);
                    if(!currCity.equalsIgnoreCase(prevCity)) {
                        System.out.println("------" + currCity + "------");
                    }
                    if (res2.getInt(1) < 2) {
                        continue;
                    }
                    System.out.println("Name: " + res2.getString(3));
                    System.out.println("Count: " + res2.getInt(1));
                    System.out.println("---");
                }
                return true;
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean topCancellations(Connection conn, Scanner reader) {
        try {
            Statement st = conn.createStatement();
            String renters = "SELECT count, sin FROM Cancellations, Renters WHERE canceller_sin = Renters.sin ORDER BY count DESC";
            String hosts = "SELECT count, sin FROM Cancellations, Hosts WHERE canceller_sin = Hosts.sin ORDER BY count DESC";
            ResultSet maxR = st.executeQuery(renters);
            ResultSet maxH = st.executeQuery(hosts);

            if(maxR.next()) {
                String nameQuery = "SELECT full_name FROM Users WHERE sin = " +  maxR.getInt(2);
                ResultSet n = st.executeQuery(nameQuery);
                if (n.next()) {
                    System.out.println("-----RENTER WITH THE MOST CANCELLATIONS-----");
                    System.out.println("Name: " + n.getString(1));
                    System.out.println("Cancellation Count: " + maxR.getInt(1));
                    System.out.println("-------");
                }
            }

            if(maxH.next()) {
                String nameQuery = "SELECT full_name FROM Users WHERE sin = " +  maxH.getInt(2);
                ResultSet n = st.executeQuery(nameQuery);
                if(n.next()) {
                    System.out.println("-----HOST WITH THE MOST CANCELLATIONS-----");
                    System.out.println("Name: " + n.getString(1));
                    System.out.println("Cancellation Count: " + maxH.getInt(1));
                    System.out.println("-------");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean popularPhrases(Connection conn, Scanner reader) {
        try {
            String phraseQuery = "SELECT * FROM ListingReviews";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(phraseQuery);

            HashMap<Integer, HashMap<String, Integer>> listingComments = new HashMap<>();
            while(res.next()) {
                int lid = res.getInt(2);
                String commentsPre = res.getString(3).toLowerCase().replaceAll("[^a-z ]", "");

                // Iterate through each of the words in the comment
                for(String word : commentsPre.split(" ")) {
                    if(word.length() <= 3) {
                        continue;
                    }

                    // Add the word into the hash map if it does not already exist
                    // If it already exists, just increment the word's counter
                    if (listingComments.get(lid) != null) {
                        if (listingComments.get(lid).get(word) != null) {
                            int v = listingComments.get(lid).get(word);
                            listingComments.get(lid).put(word, v+1);
                        } else {
                            listingComments.get(lid).put(word, 1);
                        }
                    } else {
                        HashMap<String, Integer> newMap = new HashMap<>();
                        newMap.put(word, 1);
                        listingComments.put(lid, newMap);
                    }
                }

                Iterator hmIterator = listingComments.entrySet().iterator();
                while(hmIterator.hasNext()) {
                    Map.Entry pair = (Map.Entry)hmIterator.next();
                    System.out.println("-----LISTING " + pair.getKey() + "-----" );
                    Iterator hmIterator2 = ((Map)pair.getValue()).entrySet().iterator();
                    while(hmIterator2.hasNext()) {
                        pair = (Map.Entry)hmIterator2.next();
                        System.out.println(pair.getKey() + " : " + pair.getValue() +  " occurrences");
                        System.out.println("------");
                        hmIterator2.remove();
                    }
                    hmIterator.remove();
                }
            }
            return true;
        } catch(SQLException e) {
            System.err.println("[ERROR] : Unable to get the report");
            e.printStackTrace();
        }
        return false;
    }

}

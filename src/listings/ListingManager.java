package listings;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ListingManager {

    public static boolean createListing(Connection conn, Scanner reader, int hostSin) {

        // When creating a new listing, the tables that need an update are Listings, Amenities, and Calendar
        try {
            System.out.print("Enter the listing's type (ex - house, apartment, room): ");
            String listingType = reader.nextLine();
            System.out.print("Enter the listing's latitude (ex - 123.456): ");
            String listingLat = reader.nextLine();
            System.out.print("Enter the listing's longitude (ex - 123.456): ");
            String listingLong = reader.nextLine();
            System.out.print("Enter the listing's postal code (ex - M1G 5T6): ");
            String postalCode = reader.nextLine();
            System.out.print("Enter the listing's country: ");
            String country = reader.nextLine();
            System.out.print("Enter the listing's city: ");
            String city = reader.nextLine();

            System.out.print("Enter the all the amenities the listing will include (separate each amenity by ','): ");
            String[] amenities = reader.nextLine().split(",");

            System.out.print("Enter the dates that this listing will be available on (YYYY-MM-DD,price --> separate each entry by ';'): ");
            String[] datesAndPrices = reader.nextLine().split(";");

            // Check if each specified date and price is formatted correctly and is valid
            List<String> dapList = Arrays.asList(datesAndPrices);
            for(String dap : dapList) {
                String[] datePrice =  dap.split(",");
                List<String> datePriceList = Arrays.asList(datePrice);
                if (!(datePriceList.get(0).matches("\\d{4}-\\d{2}-\\d{2}") && datePriceList.get(1).matches("^\\d{0,8}(\\.\\d{1,4})?$"))) {
                    System.err.println("[ERROR] : Invalid date/price was specified");
                    return false;
                }
            }

            // Insert the listing into the Listings table
            String insertListing = "INSERT INTO Listings (sin_host, listing_type, latitude, longitude, postal_code," +
                    " city, country) VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pS = conn.prepareStatement(insertListing);
            pS.setInt(1, hostSin);
            pS.setString(2, listingType);
            pS.setDouble(3, Double.parseDouble(listingLat));
            pS.setDouble(4, Double.parseDouble(listingLong));
            pS.setString(5, postalCode);
            pS.setString(6, city);
            pS.setString(7, country);

            try {
                pS.execute();
            } catch (SQLIntegrityConstraintViolationException e) {
                System.err.println("[ERROR] : You can't specify the same date more than once for a single listing");
                e.printStackTrace();
                return false;
            }

            // Insert the listing and its amenities into the amenities table
            // Get ID of the recently inserted row
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT last_insert_id() AS last_id FROM Listings");
            rs.next();
            String lid = rs.getString("last_id");

            for(String amenity : amenities) {
                String insertAmenity = "INSERT INTO Amenities(amenity_type, listing_id) VALUES(?, ?)";
                pS = conn.prepareStatement(insertAmenity);
                pS.setString(1, amenity);
                pS.setInt(2, Integer.parseInt(lid));
                pS.execute();
            }

            // Insert the listing's dates into the calendar table
            for(String dap : dapList) {
                String[] datePrice =  dap.split(",");
                List<String> datePriceList = Arrays.asList(datePrice);
                String insertDatesAndPrices = "INSERT INTO Calendar (calendar_date, price, listing_id, status, sin_host)" +
                        " VALUES(?, ?, ?, ?, ?)";
                pS = conn.prepareStatement(insertDatesAndPrices);
                pS.setString(1, datePriceList.get(0));
                pS.setDouble(2, Double.parseDouble(datePriceList.get(1)));
                pS.setInt(3, Integer.parseInt(lid));
                pS.setBoolean(4, true);
                pS.setInt(5, hostSin);
                pS.execute();
            }
            System.out.println("[SUCCESS] : The listing has been successfully created with the ID: " + lid);
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to create the specified listing. Ensure your formatting is correct.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean bookListing(Connection conn, Scanner reader, int renterSin, String paymentInfo) {

        // When booking a listing, the tables that need to be updated are Listings, Calendar, and History
        try {
            System.out.print("Enter the ID of the listing you wish to book: ");
            String listingID = reader.nextLine();

            // Display all the available dates of the given listing to the user
            String getDates = "SELECT calendar_date, price, status FROM Calendar WHERE listing_id = " + listingID + "";
            Statement st = conn.createStatement();
            ResultSet resDates = st.executeQuery(getDates);

            if (!resDates.next()) {
                System.err.println("[ERROR] : There are no dates available for the specified listing");
                return false;
            } else {
                System.out.println("Available Dates for Listing ID: " + listingID + "");
                System.out.println("---------------");

                // Get the data from the first row and print those out
                String lDate = resDates.getString(1);
                double lPrice = resDates.getDouble(2);
                boolean lStatus = resDates.getBoolean(3);
                System.out.println("Listing Date: " + lDate);
                System.out.println("Listing Price: $" + lPrice);
                System.out.println("Availability: " + (lStatus ? "Available" : "Booked"));
                System.out.println("---------------");

                // Iterate through the rest of the results
                while (resDates.next()) {
                    lDate = resDates.getString(1);
                    lPrice = resDates.getDouble(2);
                    lStatus = resDates.getBoolean(3);
                    System.out.println("Listing Date: " + lDate);
                    System.out.println("Listing Price: $" + lPrice);
                    System.out.println("Availability: " + (lStatus ? "Available" : "Booked"));
                    System.out.println("---------------");
                }
            }

            System.out.print("Enter the dates you wish to book (YYYY-MM-DD; separate each date by ','): ");
            String datesToBook = reader.nextLine();
            String[] dateArray = datesToBook.split(",");
            List<String> dates = Arrays.asList(dateArray);

            for(String date : dates) {
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.err.println("[ERROR] : One of your dates was incorrectly formatted. Please try again");
                    return false;
                }
            }

            int datesBooked = 0;
            for(String date : dates) {
                resDates = st.executeQuery(getDates);
                while(resDates.next()) {
                    String lDate = resDates.getString(1);
                    if(date.equalsIgnoreCase(lDate) && resDates.getBoolean(3)) {
                        // Update the Calendar Table
                        String updateCalendar = "UPDATE Calendar SET status = " +
                                false + ", sin_renter = " + renterSin + " WHERE listing_id = " + listingID + " AND calendar_date = '" + lDate + "'";
                        st = conn.createStatement();
                        st.executeUpdate(updateCalendar);

                        // Insert the record into the history table if it does not already exist
                        // First, get the host's SIN
                        String hostID = "SELECT sin_host FROM Listings WHERE listing_id = " + listingID + "";
                        st = conn.createStatement();
                        ResultSet res = st.executeQuery(hostID);
                        res.next();
                        int hostSin = res.getInt(1);

                        // Log the booking action in the history table if the date did not already exist inside the table
                        SimpleDateFormat cDate = new SimpleDateFormat("yyyy-MM-dd");
                        String cD = cDate.format(new Date());
                        String checkHistory = "SELECT * FROM History WHERE booking_date = '" + lDate +
                                "' AND listing_id = " + Integer.parseInt(listingID) +" ";
                        res = st.executeQuery(checkHistory);

                        System.out.println(res.next());
                        System.out.println(checkHistory);
                        res = st.executeQuery(checkHistory);
                        if (res.next()) {
                            String updateHistory = "UPDATE History SET action = 'booked', action_date = '" + cD + "' WHERE " +
                                    "listing_id = " + Integer.parseInt(listingID) + " AND sin_renter = " + renterSin +
                                    " AND sin_host = " + hostSin + " AND booking_date = '" + date +"'" ;
                            st.executeUpdate(updateHistory);
                        } else {
                            String insertHistory = "INSERT INTO History (listing_id, sin_renter, sin_host, booking_date, " +
                                    "action_date, action, payment_info) VALUES(?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement ps = conn.prepareStatement(insertHistory);

                            ps.setInt(1, Integer.parseInt(listingID));
                            ps.setInt(2, renterSin);
                            ps.setInt(3, hostSin);
                            ps.setString(4, lDate);
                            ps.setString(5, cD);
                            ps.setString(6, "booked");
                            ps.setString(7, paymentInfo);
                            ps.execute();
                        }
                        System.out.println("[SUCCESS] : Listing " + listingID + " has been successfully booked for " +
                                lDate + "");
                        datesBooked++;
                    }
                }
            }
            if (datesBooked == 0) {
                System.out.println("[INFO] : None of the specified dates exist in the available bookings");
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to book the specified listing");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean cancelBooking(Connection conn, Scanner reader, int sin) {
        try {
            System.out.print("Enter the ID of the listing: ");
            String idList = reader.nextLine();
            int listing = -1;
            try {
                listing = Integer.parseInt(idList);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] : Invalid listing was specified");
                return false;
            }

            // Display the bookings associated with the listing (only for the given user sin)
            String getListing = "SELECT * FROM Calendar WHERE listing_id = " + listing + " AND" +
                    " (sin_renter = " + sin + " OR sin_host = " + sin + ")";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListing);

            // If the specified listing is not associated with the account, then we end it
            if(!res.next()) {
                System.err.println("[ERROR] : The specified listing has no affiliation with your account");
                return false;
            }

            // Print out the bookings associated with the account
            String checkRenter = "SELECT * FROM Renters WHERE sin = " + sin + "";
            res = st.executeQuery(checkRenter);

            String getBookings = "";

            // If it's a renter or host...
            int renterSin = -1;
            int hostSin = -1;
            if(res.next()) {
                getBookings = "SELECT * FROM Calendar WHERE listing_id = " + listing + " " +
                        "AND status = 0 AND sin_renter = " + sin + "";
            } else {
                getBookings = "SELECT * FROM Calendar WHERE listing_id = " + listing + " " +
                        "AND status = 0";
            }
            res = st.executeQuery(getBookings);

            System.out.println("------ CURRENT BOOKINGS -----");
            while(res.next()) {
                String date = res.getString(1);
                double price = res.getDouble(2);
                renterSin =  res.getInt(5);
                hostSin = res.getInt(6);

                System.out.println("Listing Date: " + date);
                System.out.println("Price: $" + price);
                System.out.println("--------------------------");
            }

            System.out.print("Enter the date(s) of the booking(s) you wish to cancel (YYYY-MM-DD; separated by ','): ");
            String datesToCancel  = reader.nextLine();
            String[] dateArray = datesToCancel.split(",");
            List<String> dates = Arrays.asList(dateArray);
            for(String date : dates) {
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println("[INFO] : Skipping " + date + " due to incorrect formatting");
                    continue;
                }

                // Update the calendar's status and renter
                String updateCalendar = "UPDATE Calendar SET status = " +
                        true + " WHERE calendar_date = '" + date + "' AND listing_id = " + listing + "";
                st = conn.createStatement();
                st.executeUpdate(updateCalendar);

                updateCalendar = "UPDATE Calendar SET sin_renter = " +
                        null + " WHERE calendar_date = '" + date + "' AND listing_id = " + listing + " ";
                st.executeUpdate(updateCalendar);

                // Update the cancellation table
                String checkExists = "SELECT * FROM Cancellations WHERE canceller_sin = " + sin + "";
                ResultSet check = st.executeQuery(checkExists);

                // If the user has never cancelled a booking before, create the entry in the table
                if(!check.next()) {
                    String insertHistory = "INSERT INTO Cancellations (canceller_sin) " +
                            "VALUES(?)";
                    PreparedStatement ps = conn.prepareStatement(insertHistory);
                    ps.setInt(1, sin);
                    ps.execute();
                }

                String updateCancellation = "UPDATE Cancellations SET count = count + 1 WHERE canceller_sin = " + sin + "";
                st.executeUpdate(updateCancellation);

                // Update the cancelling action in the history table
                SimpleDateFormat cDate = new SimpleDateFormat("yyyy-MM-dd");
                String cD = cDate.format(new Date());
                String updateHistory = "UPDATE History SET action = 'cancelled', action_date = '" + cD + "' WHERE " +
                        "listing_id = " + listing + " AND sin_renter = " + renterSin + " AND sin_host = " + hostSin +
                        " AND booking_date = '" + date + "'";
                int updated = st.executeUpdate(updateHistory);

                // if we find that a number of rows were affected, print out a success message
                if (updated != 0) {
                    System.out.println("[SUCCESS] : The booking for " + date + " has been successfully cancelled");
                } else {
                    System.out.println("[INFO] : The booking for " + date + " does not exist for the given listing id");
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to cancel the specified booking");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateListingPrice(Connection conn, Scanner reader, int hostSin) {
        try {
            System.out.print("Enter the ID of the listing: ");
            String idList = reader.nextLine();
            int listing = -1;
            try {
                listing = Integer.parseInt(idList);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] : Invalid listing was specified");
                return false;
            }

            // Display the bookings associated with the listing (only for the given host sin)
            String getListing = "SELECT * FROM Listings WHERE listing_id = " + listing + " AND sin_host = " + hostSin + "";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListing);

            // If the specified listing is not associated with the account, then we end it
            if(!res.next()) {
                System.err.println("[ERROR] : The specified listing has no affiliation with your account");
                return false;
            }

            // Print out the bookings associated with the account
            String getBookings = "SELECT calendar_date, price, status FROM Calendar WHERE listing_id = " + listing + "";
            res = st.executeQuery(getBookings);

            System.out.println("------ CURRENT BOOKINGS -----");
            while(res.next()) {
                String date = res.getString(1);
                double price = res.getDouble(2);
                boolean availability = res.getBoolean(3);

                System.out.println("Listing Date: " + date);
                System.out.println("Price: $" + price);
                System.out.println("Availability: " + (availability ? "Available" : "Unavailable"));
                System.out.println("--------------------------");
            }

            System.out.print("Enter the date(s) of the booking(s) you wish to update (YYYY-MM-DD; separated by ','): ");
            String datesToUpdate  = reader.nextLine();
            String[] dateArray = datesToUpdate.split(",");
            List<String> dates = Arrays.asList(dateArray);

            // Iterate through each specified date and update it accordingly
            List<String> updatedDates = new ArrayList<String>();
            for(String date : dates) {
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println("[INFO] : Skipping " + date + " due to incorrect formatting");
                    continue;
                }

                // Check the booking's status
                String getBookingStatus = "SELECT status FROM Calendar WHERE listing_id = " +
                        listing + " AND calendar_date = '" + date + "'";
                res = st.executeQuery(getBookingStatus);
                if(res.next()) {
                    if (!res.getBoolean(1)) {
                        System.out.println("[INFO] : The booking on " + date + " is currently booked. You cannot change" +
                                " the price of a date that is booked.");
                        continue;
                    }
                }

                // Update the price for the current date
                System.out.print("Enter the new price for the booking on " + date + ": ");
                String updatedPrice  = reader.nextLine();

                if (!updatedPrice.matches("^\\d{0,8}(\\.\\d{1,4})?$")) {
                    System.err.println("[ERROR] : Invalid price was specified. Moving on to the next date");
                    continue;
                }

                String updatePrice =  "UPDATE Calendar SET price = " + updatedPrice + " WHERE calendar_date = '" +
                        date + "' AND listing_id = " + listing + "";
                st = conn.createStatement();
                st.executeUpdate(updatePrice);

                updatedDates.add(date);
            }

            System.out.println("[INFO] : The following dates have been updated: ");
            for(String date : dates) {
                System.out.println(date);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to update the specified listing's price");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateListingAvailability(Connection conn, Scanner reader, int hostSin) {
        try {
            System.out.print("Enter the ID of the listing: ");
            String idList = reader.nextLine();
            int listing = -1;
            try {
                listing = Integer.parseInt(idList);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] : Invalid listing was specified");
                return false;
            }

            // Display the bookings associated with the listing (only for the given host sin)
            String getListing = "SELECT * FROM Listings WHERE listing_id = " + listing + " AND sin_host = " + hostSin + "";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListing);

            // If the specified listing is not associated with the account, then we end it
            if(!res.next()) {
                System.err.println("[ERROR] : The specified listing has no affiliation with your account");
                return false;
            }

            // Print out the bookings associated with the account
            String getBookings = "SELECT calendar_date, price, status FROM Calendar WHERE listing_id = " + listing + "";
            res = st.executeQuery(getBookings);

            System.out.println("------ CURRENT BOOKINGS -----");
            while(res.next()) {
                String date = res.getString(1);
                double price = res.getDouble(2);
                boolean availability = res.getBoolean(3);

                System.out.println("Listing Date: " + date);
                System.out.println("Price: $" + price);
                System.out.println("Availability: " + (availability ? "Available" : "Unavailable"));
                System.out.println("--------------------------");
            }

            System.out.print("Enter the date(s) of the booking(s) you wish to update (YYYY-MM-DD; separated by ','): ");
            String datesToUpdate  = reader.nextLine();
            String[] dateArray = datesToUpdate.split(",");
            List<String> dates = Arrays.asList(dateArray);

            // Iterate through each specified date and update it accordingly
            List<String> updatedDates = new ArrayList<String>();
            for(String date : dates) {
                if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    System.out.println("[INFO] : Skipping " + date + " due to incorrect formatting");
                    continue;
                }

                // Check the booking's status
                String getBookingStatus = "SELECT status FROM Calendar WHERE listing_id = " +
                        listing + " AND calendar_date = '" + date + "'";
                res = st.executeQuery(getBookingStatus);
                if(res.next()) {
                    if (!res.getBoolean(1)) {
                        System.out.println("[INFO] : The booking on " + date + " is currently unavailable. You cannot change" +
                                " the availability of a date that is unavailable.");
                        continue;
                    }
                }

                // Update the availability for the current date --> updates to Unavailable should be fine
                String updatePrice =  "UPDATE Calendar SET status = " + false + " WHERE calendar_date = '" +
                        date + "' AND listing_id = " + listing + "";
                st = conn.createStatement();
                st.executeUpdate(updatePrice);

                updatedDates.add(date);
            }

            System.out.println("[INFO] : The following dates have been updated: ");
            for(String date : dates) {
                System.out.println(date);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to update the specified listing's availability");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeListing(Connection conn, Scanner reader, int hostSin) {
        try {
            System.out.print("Enter the ID of the listing you wish to remove: ");
            String idList = reader.nextLine();
            int listing = -1;
            try {
                listing = Integer.parseInt(idList);
            } catch (NumberFormatException e) {
                System.err.println("[ERROR] : Invalid listing was specified");
                return false;
            }

            // If the specified listing is not associated with the account, then we end it
            String getListing = "SELECT * FROM Listings WHERE listing_id = " + listing + " AND sin_host = " + hostSin + "";
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery(getListing);

            if(!res.next()) {
                System.err.println("[ERROR] : The specified listing has no affiliation with your account");
                return false;
            }

            // Delete the listing
            String deleteListing = "DELETE FROM Listings WHERE listing_id = " + listing + "";
            st = conn.createStatement();
            st.executeUpdate(deleteListing);

            System.out.println("[SUCCESS] : The specified has been deleted");
            return true;

        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to remove the specified listing");
            e.printStackTrace();
        }
        return false;
    }

}

package listings;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class CommentManager {

    public static boolean reviewRenter(Connection conn, Scanner reader, int hostSin) {
        try {
            // Print out the renters associated with the host's listings

            // Query for renters that have rented from the given host (and the listing associated with them) in addition
            // to making sure the renter has completed a stay
            Statement st = conn.createStatement();
            SimpleDateFormat cDate = new SimpleDateFormat("yyyy-MM-dd");
            String cD = cDate.format(new Date());
            String getRenters = "SELECT sin_renter FROM History WHERE sin_host = " + hostSin +
                    " AND booking_date < CURDATE() AND action = 'booked'";
            ResultSet res = st.executeQuery(getRenters);

            ArrayList<Integer> renterSins = new ArrayList<Integer>();
            while(res.next()) {
                renterSins.add(res.getInt(1));
            }

            HashMap<String, Integer> renters = new HashMap<String, Integer>();
            for(Integer renterSin : renterSins) {
                String getRenter = "SELECT full_name FROM Users WHERE sin = " + renterSin + " ";
                res = st.executeQuery(getRenter);
                if(res.next()) {
                    renters.put(res.getString(1), renterSin);
                    System.out.println("Renter: " + res.getString(1));
                }
            }

            if(renters.isEmpty()) {
                System.out.println("[INFO] : No one has rented from any of your listings");
                return false;
            }

            System.out.print("Enter the name of the renter you wish to write a review for: ");
            String name = reader.nextLine();

            if(!renterSins.contains(renters.get(name))) {
                System.out.println("[ERROR] : That person does not exist");
                return false;
            }

            System.out.print("Rate the renter on a scale from 1 - 5: ");
            String rating = reader.nextLine();

            if (!rating.matches("[1-5]")) {
                System.out.println("[ERROR] : Invalid rating");
                return false;
            }

            System.out.print("Provide a review of the Renter (MAX of 1000 characters): ");
            String review = reader.nextLine();

            String insertReview = "INSERT INTO UserReviews (creator_id, target_user_id, content, rating) VALUES(?, ?, ?, ?)";
            PreparedStatement pS = conn.prepareStatement(insertReview);
            pS.setInt(1, hostSin);
            pS.setInt(2, renters.get(name));
            pS.setString(3, review);
            pS.setInt(4, Integer.parseInt(rating));
            pS.execute();
            System.out.println("[SUCCESS] : Your review has been submitted for the renter " + name);
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to comment on the specified renter");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean reviewHostListing(Connection conn, Scanner reader, int renterSin) {
        try {

            // Query for hosts that the user has rented from
            Statement st = conn.createStatement();
            SimpleDateFormat cDate = new SimpleDateFormat("yyyy-MM-dd");
            String cD = cDate.format(new Date());
            String getRenter = "SELECT sin_host, listing_id FROM History WHERE sin_renter = " + renterSin +
                    " AND booking_date < CURDATE() AND action = 'booked'";
            ResultSet res = st.executeQuery(getRenter);

            ArrayList<Integer> hostSins = new ArrayList<Integer>();
            ArrayList<Integer> listings = new ArrayList<Integer>();
            while(res.next()) {
                hostSins.add(res.getInt(1));
                listings.add(res.getInt(2));
            }

            HashMap<String, Integer> hosts = new HashMap<String, Integer>();
            for(Integer hostSin : hostSins) {
                String getHost = "SELECT full_name FROM Users WHERE sin = " + hostSin + " ";
                res = st.executeQuery(getHost);
                if(res.next()) {
                    hosts.put(res.getString(1), hostSin);
                    System.out.println("Host: " + res.getString(1));
                }
            }

            if(hosts.isEmpty()) {
                System.out.println("[INFO] : You haven't rented anything from anyone");
                return false;
            }

            System.out.print("Enter the name of the host you wish to write a review for: ");
            String name = reader.nextLine();

            if(!hostSins.contains(hosts.get(name))) {
                System.err.println("[ERROR] : That person does not exist");
                return false;
            }

            System.out.print("Rate the host on a scale from 1 - 5: ");
            String rating = reader.nextLine();

            if (!rating.matches("[1-5]")) {
                System.err.println("[ERROR] : Invalid rating");
                return false;
            }

            System.out.print("Provide a review of the Host (MAX of 1000 characters): ");
            String reviewHost = reader.nextLine();

            System.out.print("Enter the ID of the listing you wish to write a review for: ");
            String listID = reader.nextLine();

            if(!listings.contains(Integer.parseInt(listID))) {
                System.err.println("[ERROR] : That listing does not exist with the associated host");
                return false;
            }

            System.out.print("Rate the listing on a scale from 1 - 5: ");
            String listRating = reader.nextLine();

            if (!listRating.matches("[1-5]")) {
                System.err.println("[ERROR] : Invalid rating");
                return false;
            }

            System.out.print("Provide a review of the Host (MAX of 1000 characters): ");
            String reviewListing = reader.nextLine();

            String insertReviewHost = "INSERT INTO UserReviews (creator_id, target_user_id, content, rating) VALUES(?, ?, ?, ?)";
            PreparedStatement pS1 = conn.prepareStatement(insertReviewHost);
            pS1.setInt(1, renterSin);
            System.out.println(hosts.get(name));
            pS1.setInt(2, hosts.get(name));
            pS1.setString(3, reviewHost);
            pS1.setInt(4, Integer.parseInt(rating));

            String insertReviewListing = "INSERT INTO ListingReviews (creator_id, target_listing_id, content, rating) VALUES(?, ?, ?, ?)";
            PreparedStatement pS2 = conn.prepareStatement(insertReviewListing);
            pS2.setInt(1, renterSin);
            pS2.setInt(2, Integer.parseInt(listID));
            pS2.setString(3, reviewListing);
            pS2.setInt(4, Integer.parseInt(listRating));

            pS1.execute();
            pS2.execute();

            System.out.println("[SUCCESS] : Your review has been submitted for the renter " + name);
            return true;
        } catch (SQLException e) {
            System.err.println("[ERROR] : Unable to comment on the specified host");
            e.printStackTrace();
        }
        return false;
    }
}

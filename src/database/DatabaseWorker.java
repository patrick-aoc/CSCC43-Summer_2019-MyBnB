package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseWorker {

    /**
     * Create all the necessary tables in the database
     * @param conn The Database connection instance
     */
    public static void createTables(Connection conn) {
        try {
            Statement st = conn.createStatement();
            String sql = "";

            // Begin by dropping all the existing tables
            sql = "DROP TABLE IF EXISTS Cancellations CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS UserReviews CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS ListingReviews CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Reviews CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS History CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Calendar CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Amenities CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Listings CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Hosts CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Renters CASCADE";
            st.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS Users CASCADE";
            st.executeUpdate(sql);

            // Create the Users table and the age check trigger
            sql = "CREATE TABLE Users " +
                    "(full_name VARCHAR(100)," +
                    "date_of_birth DATE NOT NULL," +
                    "occupation VARCHAR(200)," +
                    "sin INT NOT null," +
                    "username VARCHAR(50) UNIQUE," +
                    "password VARCHAR(50)," +
                    "address VARCHAR(200)," +
                    "PRIMARY KEY ( sin )" +
                    ")";
            st.executeUpdate(sql);
            sql = "CREATE TRIGGER date_check BEFORE INSERT ON Users " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "IF DATEDIFF(CURDATE(), NEW.date_of_birth) < 18 * 365 THEN " +
                    "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'You must be older than 18 to use this application!';" +
                    "END IF; " +
                    "END";
            st.execute(sql);

            // Create the Renters table and ensure the sin is the primary key
            sql = "CREATE TABLE IF NOT EXISTS Renters " +
                    "(credit_card_num VARCHAR(50)," +
                    "sin INT NOT null," +
                    "PRIMARY KEY ( sin )," +
                    "FOREIGN KEY ( sin ) REFERENCES Users( sin ) ON DELETE CASCADE )";
            st.executeUpdate(sql);

            // Create the Hosts table and ensure the sin is the primary key
            sql = "CREATE TABLE IF NOT EXISTS Hosts " +
                    "(sin INT NOT null," +
                    "PRIMARY KEY ( sin )," +
                    "FOREIGN KEY ( sin ) REFERENCES Users( sin ) ON DELETE CASCADE )";
            st.executeUpdate(sql);

            // Create the Listings table and check to see if the latitude/longitude are valid numbers
            sql = "CREATE TABLE IF NOT EXISTS Listings " +
                    "(sin_host INT NOT NULL," +
                    "listing_type VARCHAR(200)," +
                    "listing_id INT NOT NULL AUTO_INCREMENT," +
                    "latitude DECIMAL(9,6) NOT NULL," +
                    "longitude DECIMAL(9,6) NOT NULL," +
                    "postal_code VARCHAR(7)," +
                    "city VARCHAR(50)," +
                    "country VARCHAR(50)," +
                    "PRIMARY KEY ( listing_id, sin_host, latitude, longitude )," +
                    "FOREIGN KEY ( sin_host ) REFERENCES Hosts( sin ) ON DELETE CASCADE," +
                    "CHECK (latitude >= -90 AND latitude <= 90)," +
                    "CHECK (longitude >= -180 AND longitude <= 180)" +
                    ")";
            st.executeUpdate(sql);

            // Create the Amenities table
            sql = "CREATE TABLE IF NOT EXISTS Amenities " +
                    "(amenity_type VARCHAR(100)," +
                    "listing_id INT NOT NULL," +
                    "PRIMARY KEY ( amenity_type, listing_id )," +
                    "FOREIGN KEY ( listing_id ) REFERENCES Listings( listing_id ) ON DELETE CASCADE)";
            st.executeUpdate(sql);

            // Create the Calendar table
            sql = "CREATE TABLE IF NOT EXISTS Calendar " +
                    "(calendar_date DATE NOT NULL," +
                    "price REAL NOT NULL," +
                    "listing_id INT NOT NULL," +
                    "status BOOLEAN," +
                    "sin_renter INT DEFAULT NULL," +
                    "sin_host INT," +
                    "PRIMARY KEY ( listing_id, calendar_date )," +
                    "FOREIGN KEY ( listing_id ) REFERENCES Listings( listing_id ) ON DELETE CASCADE)";
            st.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS History " +
                    "(log_id INT NOT NULL AUTO_INCREMENT," +
                    "listing_id INT NOT NULL," +
                    "sin_renter INT NOT NULL," +
                    "sin_host INT NOT NULL," +
                    "booking_date DATE," +
                    "action_date DATE," +
                    "action VARCHAR(15)," +
                    "payment_info VARCHAR(50)," +
                    "PRIMARY KEY ( log_id )," +
                    "FOREIGN KEY ( sin_renter ) REFERENCES Renters( sin ) ON DELETE CASCADE," +
                    "FOREIGN KEY ( sin_host ) REFERENCES  Hosts ( sin ) ON DELETE CASCADE," +
                    "FOREIGN KEY ( listing_id ) REFERENCES Listings( listing_id ) ON DELETE CASCADE)";
            st.executeUpdate(sql);

            // Create the Reviews table and check to make sure the rating is a valid number and the reviewer is not
            // reviewing themselves
            sql = "CREATE TABLE IF NOT EXISTS UserReviews " +
                    "(creator_id INT NOT NULL, " +
                    "target_user_id INT NOT NULL, " +
                    "content VARCHAR(1000), " +
                    "rating INT NOT NULL, " +
                    "review_id INT NOT NULL AUTO_INCREMENT, " +
                    "PRIMARY KEY ( review_id ), " +
                    "FOREIGN KEY ( creator_id ) REFERENCES Users( sin ) ON DELETE CASCADE, " +
                    "FOREIGN KEY ( target_user_id ) REFERENCES Users( sin ) ON DELETE CASCADE, " +
                    "CHECK (rating > 0 AND rating < 6)," +
                    "CHECK (creator_id <> target_user_id)" +
                    ")";
            st.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS ListingReviews " +
                    "(creator_id INT NOT NULL, " +
                    "target_listing_id INT, " +
                    "content VARCHAR(1000), " +
                    "rating INT NOT NULL, " +
                    "review_id INT NOT NULL AUTO_INCREMENT, " +
                    "PRIMARY KEY ( review_id ), " +
                    "FOREIGN KEY ( target_listing_id ) REFERENCES Listings( listing_id ) ON DELETE CASCADE, " +
                    "CHECK (rating > 0 AND rating < 6)," +
                    "CHECK (creator_id <> target_listing_id)" +
                    ")";
            st.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS Cancellations " +
                    "(canceller_sin INT NOT NULL," +
                    "count INT NOT NULL DEFAULT 0," +
                    "PRIMARY KEY ( canceller_sin )," +
                    "FOREIGN KEY ( canceller_sin ) REFERENCES Users( sin ) ON DELETE CASCADE)";
            st.execute(sql);

            System.out.println("[SUCCESS] : Database has been successfully initialized");
        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to add the specified tables");
            e.printStackTrace();
        }
    }

    public static void loadSampleData(Connection conn) {
        try {
            Statement st = conn.createStatement();
            String insertUsers = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Users.csv" + "' INTO TABLE Users FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
            st.executeUpdate(insertUsers);

            String insertRenters = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Renters.csv" + "' INTO TABLE Renters FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
            st.executeUpdate(insertRenters);

            String insertHosts = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Hosts.csv" +
                    "' INTO TABLE Hosts FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
            st.executeUpdate(insertHosts);

            String insertListings = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Listings.csv" +
                    "' INTO TABLE Listings FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
            st.executeUpdate(insertListings);

            String insertCalendar = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Calendar.csv" +
                    "' INTO TABLE Calendar FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'"  + " (calendar_date, price, listing_id, status, @vsin_renter, sin_host) " +
                    " SET sin_renter = nullif(@vsin_renter, 'NULL')";
            st.executeUpdate(insertCalendar);

            String insertAmenities = "LOAD DATA LOCAL INFILE '" + "C://Users/patri/Documents/CSCC43/MyBnB/data/mybnb - Amenities.csv" +
                    "' INTO TABLE Amenities FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
            st.executeUpdate(insertAmenities);

            System.out.println("[SUCCESS] : The data files have been successfully loaded into the database");
        } catch(SQLException e) {
            System.err.println("[ERROR] Unable to load the sample data into the tables");
            e.printStackTrace();
        }
    }
}

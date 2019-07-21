package users;

import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class AccountManager {

    public static User createUser(Connection conn, Scanner reader) {
        try {
            System.out.print("Enter your date of birth (YYYY-MM-DD): ");
            String dob = reader.nextLine();

            if (dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
                int year = Integer.parseInt(dob.substring(0, 4));
                if (Calendar.getInstance().get(Calendar.YEAR) - year >= 18) {
                    System.out.print("Enter your first and last name: ");
                    String fullName = reader.nextLine();
                    System.out.print("Enter your address: ");
                    String address = reader.nextLine();
                    System.out.print("Enter your occupation: ");
                    String occupation = reader.nextLine();
                    System.out.print("Enter your SIN number: ");
                    String sin = reader.nextLine();

                    // If the sin contains letters or is less than 9 characters, it is invalid
                    if (sin.length() < 9 || sin.contains("[a-zA-Z]+") == true) {
                        System.err.println("[ERROR] : Invalid SIN");
                        return null;
                    }
                    System.out.print("Enter your username: ");
                    String username = reader.nextLine();
                    System.out.print("Enter your password: ");
                    String password = reader.nextLine();
                    System.out.print("Are you a renter or a host? ");
                    String choice = reader.nextLine();

                    if (choice.equalsIgnoreCase("renter")) {
                        System.out.print("Enter your credit card number: ");
                        String ccardNum = reader.nextLine();

                        // If the credit card number is less than 16 characters and contains letters, return null
                        if (ccardNum.length() < 16 || ccardNum.contains("[a-zA-Z]+") == true) {
                            System.out.println("Invalid Credit Card Number");
                            return null;
                        }

                        Renter user = new Renter();
                        user.setName(fullName);
                        user.setDateOfBirth(dob);
                        user.setAddress(address);
                        user.setOccupation(occupation);
                        user.setSin(Integer.parseInt(sin));
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setCreditCardNumber(ccardNum);

                        // Insert the user into their respective table
                        String userQuery = "INSERT INTO Users (full_name, date_of_birth, occupation, sin, username, " +
                                "password, address) VALUES(?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement pS = conn.prepareStatement(userQuery);
                        pS.setString(1, user.getName());
                        pS.setString(2, user.getDateOfBirth());
                        pS.setString(3, user.getOccupation());
                        pS.setInt(4, user.getSin());
                        pS.setString(5, user.getUsername());
                        pS.setString(6, user.getPassword());
                        pS.setString(7, user.getAddress());

                        try {
                            pS.execute();
                            userQuery = "INSERT INTO Renters (credit_card_num, sin) VALUES(?, ?)";
                            pS = conn.prepareStatement(userQuery);
                            pS.setString(1, user.getCreditCardNumber());
                            pS.setInt(2, user.getSin());
                            pS.execute();
                        } catch (SQLIntegrityConstraintViolationException e) {
                            System.err.println("[ERROR] : The specified user already exists");
                            user = null;
                        }

                        return user;
                    } else if (choice.equalsIgnoreCase("host")) {
                        Host user = new Host();
                        user.setName(fullName);
                        user.setDateOfBirth(dob);
                        user.setAddress(address);
                        user.setOccupation(occupation);
                        user.setSin(Integer.parseInt(sin));
                        user.setUsername(username);
                        user.setPassword(password);

                        // Insert the user into their respective table
                        // Insert the user into their respective table
                        String userQuery = "INSERT INTO Users (full_name, date_of_birth, occupation, sin, username, " +
                                "password, address) VALUES(?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement pS = conn.prepareStatement(userQuery);
                        pS.setString(1, user.getName());
                        pS.setString(2, user.getDateOfBirth());
                        pS.setString(3, user.getOccupation());
                        pS.setInt(4, user.getSin());
                        pS.setString(5, user.getUsername());
                        pS.setString(6, user.getPassword());
                        pS.setString(7, user.getAddress());

                        try {
                            pS.execute();

                            // Insert the user into their respective table
                            userQuery = "INSERT INTO Hosts (sin) VALUES(?)";
                            pS = conn.prepareStatement(userQuery);
                            pS.setInt(1, user.getSin());
                            pS.execute();
                        } catch (SQLIntegrityConstraintViolationException e) {
                            System.err.println("[ERROR] : The specified user already exists");
                            user = null;
                        }
                        return user;
                    } else {
                        System.err.println("[ERROR] : Invalid selection. Returning to the previous menu.");
                        return null;
                    }
                } else {
                    System.err.println("[ERROR] : You are too young to be using this service");
                    return null;
                }
            } else {
                System.err.println("[ERROR] : Invalid date format");
                return null;
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to create the specified user");
            e.printStackTrace();
        }
        return null;
    }

    public static User loginUser(Connection conn, Scanner reader) {
        try {
            Statement st = conn.createStatement();
            System.out.print("Enter your username: ");
            String username = reader.nextLine();
            System.out.print("Enter your password: ");
            String password = reader.nextLine();

            String userQuery = "SELECT * FROM Users " +
                    "WHERE username = '" + username + "' AND password = '" + password + "'";
            ResultSet res = st.executeQuery(userQuery);
            if (res.next()) {
                System.out.println("[SUCCESS] : User " + username + " has been successfully authenticated");

                // Check if the user is a renter or a host
                userQuery = "SELECT * FROM Renters WHERE sin = '" + res.getInt(4) + "'";
                ResultSet res2 = st.executeQuery(userQuery);
                if (res2.next()) {
                    Renter user = new Renter();
                    user.setCreditCardNumber(res2.getString(1));
                    user.setName(res.getString(1));
                    user.setDateOfBirth(res.getDate(2).toString());
                    user.setOccupation(res.getString(3));
                    user.setSin(res.getInt(4));
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setAddress(res.getString(7));
                    return user;
                } else {
                    userQuery = "SELECT * FROM Hosts WHERE sin = '" + res.getInt(4) + "'";
                    ResultSet res3 = st.executeQuery(userQuery);
                    if (res3.next()) {
                        Host user = new Host();
                        user.setName(res.getString(1));
                        user.setDateOfBirth(res.getDate(2).toString());
                        user.setOccupation(res.getString(3));
                        user.setSin(res.getInt(4));
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setAddress(res.getString(7));
                        return user;
                    }
                }
            } else {
                System.err.println("[ERROR] : Unable to login with the given credentials. Please try again");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to login the specified user");
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteUser(Connection conn, Scanner reader, int sin) {
        try {
            System.out.print("Are you sure you want to delete your account? (Y - Yes; N - No): ");
            String selection = reader.nextLine();

            if (selection.equalsIgnoreCase("y")) {
                String deleteUser = "DELETE FROM Users WHERE sin = " + sin + "";
                Statement st = conn.createStatement();
                st.executeUpdate(deleteUser);

                System.out.println("[SUCCESS] : Your account has been deleted. Good bye!");
                return true;
            } else if (selection.equalsIgnoreCase("n")) {
                return true;
            } else {
                System.err.println("[ERROR] : Invalid selection. Please try again");
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to delete your account");
            e.printStackTrace();
        }
        return false;
    }
}

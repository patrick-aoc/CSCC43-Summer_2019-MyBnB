package mybnb;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import database.DatabaseWorker;
import listings.CommentManager;
import listings.Listing;
import listings.ListingManager;
import listings.SearchManager;
import users.AccountManager;
import users.Renter;
import users.User;
import users.Host;

public class MyBnBMain {

    private static final String dbClassName = "com.mysql.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1/";

    public static void main(String[] args) {

        // Establish the initial connection to MySQL
        Connection conn = null;
        conn = connectToMySQL(conn);

        if (conn != null) {
            System.out.println("");
            System.out.println("***************************");
            System.out.println("******ACCESS GRANTED*******");
            System.out.println("***************************");
            System.out.println("");

            Scanner sc = new Scanner(System.in);
            String input = "";
            int choice = -1;
            do {

                // Initial menu before entering the MyBnB application
                Menus.initialMenu();
                input = sc.nextLine();

                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] : There was a problem with parsing your input");
                    choice = -1;
                }

                if (choice == 1) {
                    DatabaseWorker.createTables(conn);
                } else if (choice == 2) {
                    System.out.print("Please specify which table you'd like to delete: ");
                    String tableToDelete = sc.nextLine();
                    deleteTable(conn, tableToDelete);
                } else if (choice == 3) {
                    System.out.print("Please specify which table you'd like to view: ");
                    String tab = sc.nextLine();
                    printColSchema(conn, tab);
                } else if (choice == 4) {

                    // Menu in which the user is choosing between logging in or creating an account
                    String inputBnB = "";
                    int choiceBnB = -1;
                    do {

                        // Initial menu for the MyBnB application (ability to log in or create an account)
                        Menus.myBnBMenu();
                        inputBnB = sc.nextLine();

                        try {
                            choiceBnB = Integer.parseInt(inputBnB);
                        } catch (NumberFormatException e) {
                            System.err.println("[ERROR] : There was a problem with parsing your input");
                        }

                        User user = null;

                        if (choiceBnB == 1) {
                            System.out.println("===============LOGIN===============");
                            user = AccountManager.loginUser(conn, sc);
                            if (user != null) {
                                if (user instanceof Renter) {
                                    String inputRenter = "";
                                    int choiceRenter = -1;
                                    do {
                                        Menus.renterMenu();
                                        inputRenter = sc.nextLine();

                                        try {
                                            choiceRenter = Integer.parseInt(inputRenter);
                                        } catch (NumberFormatException e) {
                                            System.err.println("[ERROR] : There was a problem with parsing your input");
                                            choiceRenter = -1;
                                        }

                                        if (choiceRenter == 1) {
                                            System.out.println("===============BOOK A LISTING===============");
                                            ListingManager.bookListing(conn, sc, user.getSin(), ((Renter) user).getCreditCardNumber());
                                        } else if (choiceRenter == 2) {
                                            System.out.println("===============CANCEL A BOOKING===============");
                                            ListingManager.cancelBooking(conn, sc, user.getSin());
                                        } else if (choiceRenter == 3) {
                                            System.out.println("===============REVIEW A HOST/LISTING===============");
                                            CommentManager.reviewHostListing(conn, sc, user.getSin());
                                        } else if (choiceRenter == 4) {
                                            System.out.println("===============DELETE ACCOUNT===============");
                                            boolean deleted = AccountManager.deleteUser(conn, sc, user.getSin());
                                            if (deleted) {
                                                break;
                                            }
                                        } else {
                                            System.err.println("[ERROR] : Please enter your choice again");
                                        }
                                    } while(inputRenter.compareTo("0") != 0);
                                } else {
                                    String inputHost = "";
                                    int choiceHost = -1;
                                    do {
                                        Menus.hostMenu();
                                        inputHost = sc.nextLine();

                                        try {
                                            choiceHost = Integer.parseInt(inputHost);
                                        } catch (NumberFormatException e) {
                                            System.err.println("[ERROR] : There was a problem with parsing your input");
                                            choiceHost = -1;
                                        }

                                        if (choiceHost == 1) {
                                            System.out.println("===============CREATE A LISTING===============");
                                            ListingManager.createListing(conn, sc, user.getSin());
                                        } else if (choiceHost == 2) {
                                            System.out.println("===============UPDATE LISTING PRICE===============");
                                            ListingManager.updateListingPrice(conn, sc, user.getSin());
                                        } else if (choiceHost == 3) {
                                            System.out.println("===============UPDATE LISTING AVAILABILITY===============");
                                            ListingManager.updateListingAvailability(conn, sc, user.getSin());
                                        } else if (choiceHost == 4) {
                                            System.out.println("===============REMOVE A LISTING===============");
                                            ListingManager.removeListing(conn, sc, user.getSin());
                                        } else if (choiceHost == 5) {
                                            System.out.println("===============CANCEL A BOOKING===============");
                                            ListingManager.cancelBooking(conn, sc, user.getSin());
                                        } else if (choiceHost == 6) {
                                            System.out.println("===============REVIEW A RENTER===============");
                                            CommentManager.reviewRenter(conn, sc, user.getSin());
                                        } else if (choiceHost == 7) {
                                            System.out.println("===============DELETE ACCOUNT===============");
                                            boolean deleted = AccountManager.deleteUser(conn, sc, user.getSin());
                                            if (deleted) {
                                                break;
                                            }
                                        } else {
                                            System.err.println("[ERROR] : Please enter your choice again");
                                        }
                                    } while(inputHost.compareTo("0") != 0);
                                }
                            }
                        } else if (choiceBnB == 2) {
                            System.out.println("===============USER ACCOUNT CREATION===============");
                            user = AccountManager.createUser(conn, sc);
                            if (user != null) {
                                System.out.println("[SUCCESS] : User has been successfully created");
                            }
                        } else if (choiceBnB == 3) {

                            String inputSearch = "";
                            int choiceSearch = -1;
                            do {

                                // Initial menu for switching between what kind of search the user wants to perform
                                Menus.queriesMenu();
                                inputSearch = sc.nextLine();

                                try {
                                    choiceSearch = Integer.parseInt(inputSearch);
                                } catch (NumberFormatException e) {
                                    System.err.println("[ERROR] : There was a problem with parsing your input");
                                    choiceSearch = -1;
                                }

                                if (choiceSearch == 1) {
                                    System.out.println("===============LATITUDE/LONGITUDE SEARCH===============");
                                    SearchManager.searchByLatLong(conn, sc);
                                } else if (choiceSearch == 2) {
                                    System.out.println("===============POSTAL CODE SEARCH===============");
                                } else if (choiceSearch == 3) {
                                    System.out.println("===============ADDRESS SEARCH===============");
                                } else if (choiceSearch == 4) {
                                    System.out.println("===============DATE RANGE SEARCH===============");

                                } else if (choice == 5) {

                                } else {
                                    System.err.println("[ERROR] : Please enter your choice again");
                                }
                            } while (inputSearch.compareTo("0") != 0);
                        } else {
                            System.err.println("[ERROR] : Please enter your choice again");
                        }
                    } while (inputBnB.compareTo("0") != 0);
                }

            } while (input.compareTo("0") != 0);

        } else {
            System.out.println("");
            System.out.println("***************************");
            System.out.println("*******ACCESS DENIED*******");
            System.out.println("***************************");
            System.out.println("");
        }
    }

    /**
     * Initialize the Connection to our MySQL database for MyBnB
     * @param conn The connection to initialize
     * @return The successfully initialized connection, otherwise null
     */
    public static Connection connectToMySQL(Connection conn) {
        try {
            Class.forName(dbClassName);
            String user = "root";
            String pass = "";
            String connection = CONNECTION + "mybnb";
            try {
                conn = DriverManager.getConnection(connection, user, pass);
            } catch (SQLException e) {
                System.err.println("Connection could not be established!");
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Establishing connection triggered an exception!");
            e.printStackTrace();
        }
        return conn;
    }

    public static void deleteTable(Connection conn, String tableToDelete) {
        try {
            Statement st = conn.createStatement();
            String sql = "DROP TABLE IF EXISTS " + tableToDelete;
            st.executeUpdate(sql);

        } catch (SQLException e) {
            System.err.println("[ERROR] Unable to drop the given table");
            e.printStackTrace();
        }
    }

    //================================TESTING STUFF=====================================================
    public static void printColSchema(Connection conn, String tableName) {
        try {
            Statement st = conn.createStatement();
            System.out.print("Table Name: ");
            ArrayList<String> result = colSchema(tableName, conn);
            System.out.println("");
            System.out.println("------------");
            System.out.println("Total number of fields: " + result.size() / 2);
            for (int i = 0; i < result.size(); i += 2) {
                System.out.println("-");
                System.out.println("Field Name: " + result.get(i));
                System.out.println("Field Type: " + result.get(i + 1));
            }
            System.out.println("------------");
            System.out.println("");
        } catch(SQLException e) {
            System.out.println(":(");
        }
    }

    public static ArrayList<String> colSchema(String tableName, Connection conn) {
        ArrayList<String> result = new ArrayList<String>();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, tableName, null);
            while(rs.next()) {
                result.add(rs.getString(4));
                result.add(rs.getString(6));
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Retrieval of Table Info failed!");
            e.printStackTrace();
            result.clear();
        }
        return result;
    }
}

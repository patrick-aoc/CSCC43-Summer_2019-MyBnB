package mybnb;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import database.DatabaseWorker;
import database.ReportManager;
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
                } else if (choice == 3) {

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
                            System.out.println("================== LOGIN ===================");
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
                                            System.out.println("============ BOOK A LISTING =============");
                                            ListingManager.bookListing(conn, sc, user.getSin(), ((Renter) user).getCreditCardNumber());
                                        } else if (choiceRenter == 2) {
                                            System.out.println("=========== CANCEL A BOOKING ============");
                                            ListingManager.cancelBooking(conn, sc, user.getSin());
                                        } else if (choiceRenter == 3) {
                                            System.out.println("========= REVIEW A HOST/LISTING =========");
                                            CommentManager.reviewHostListing(conn, sc, user.getSin());
                                        } else if (choiceRenter == 4) {
                                            System.out.println("============ DELETE ACCOUNT =============");
                                            boolean deleted = AccountManager.deleteUser(conn, sc, user.getSin());
                                            if (deleted) {
                                                break;
                                            }
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
                                            System.out.println("=========== CREATE A LISTING ============");
                                            ListingManager.createListing(conn, sc, user.getSin());
                                        } else if (choiceHost == 2) {
                                            System.out.println("========== UPDATE LISTING PRICE =========");
                                            ListingManager.updateListingPrice(conn, sc, user.getSin());
                                        } else if (choiceHost == 3) {
                                            System.out.println("====== UPDATE LISTING AVAILABILITY ======");
                                            ListingManager.updateListingAvailability(conn, sc, user.getSin());
                                        } else if (choiceHost == 4) {
                                            // TODO test this feature
                                            System.out.println("======== ADD LISTING AVAILABILITY ======");
                                            ListingManager.addListingAvailability(conn, sc, user.getSin());
                                        } else if (choiceHost == 5) {
                                            System.out.println("=========== REMOVE A LISTING ===========");
                                            ListingManager.removeListing(conn, sc, user.getSin());
                                        } else if (choiceHost == 6) {
                                            System.out.println("=========== CANCEL A BOOKING ===========");
                                            ListingManager.cancelBooking(conn, sc, user.getSin());
                                        } else if (choiceHost == 7) {
                                            System.out.println("============ REVIEW A RENTER ===========");
                                            CommentManager.reviewRenter(conn, sc, user.getSin());
                                        } else if (choiceHost == 8) {
                                            System.out.println("============ DELETE ACCOUNT ============");
                                            boolean deleted = AccountManager.deleteUser(conn, sc, user.getSin());
                                            if (deleted) {
                                                break;
                                            }
                                        }
                                    } while(inputHost.compareTo("0") != 0);
                                }
                            }
                        } else if (choiceBnB == 2) {
                            System.out.println("============== USER ACCOUNT CREATION ==============");
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
                                    System.out.println("========== LATITUDE/LONGITUDE SEARCH ===========");
                                    SearchManager.searchByLatLong(conn, sc);
                                } else if (choiceSearch == 2) {
                                    System.out.println("============== POSTAL CODE SEARCH ==============");
                                    SearchManager.searchByPostalCode(conn, sc);
                                } else if (choiceSearch == 3) {
                                    System.out.println("================ ADDRESS SEARCH ================");
                                    SearchManager.searchByAddress(conn, sc);
                                } else if (choiceSearch == 4) {
                                    System.out.println("=============== DATE RANGE SEARCH ==============");
                                    SearchManager.searchByDateRange(conn, sc);
                                } else if (choiceSearch == 5) {
                                    System.out.println("================== FULL SEARCH ==================");
                                    SearchManager.searchWithFilters(conn, sc);
                                } else if (choiceSearch == 6) {
                                    System.out.println("======= FULL SEARCH WITH FILTER SELECTION =======");
                                    SearchManager.searchWithFiltersV2(conn, sc);
                                }
                            } while (inputSearch.compareTo("0") != 0);
                        } else if (choiceBnB == 4) {

                            String inputReports = "";
                            int choiceReports = -1;

                            do {

                                Menus.reportsMenu();
                                inputReports = sc.nextLine();

                                try {
                                    choiceReports = Integer.parseInt(inputReports);
                                } catch (NumberFormatException e) {
                                    System.err.println("[ERROR] : There was a problem with parsing your input");
                                    choiceReports = -1;
                                }

                                if (choiceReports == 1) {
                                    System.out.println("============== TOTAL # OF BOOKINGS BY CITY ==============");
                                    ReportManager.totalNumberOfBookings(conn, sc);
                                } else if (choiceReports == 2) {
                                    System.out.println("============ TOTAL # OF LISTINGS BY LOCATION ============");
                                    ReportManager.totalNumberOfListings(conn);
                                } else if (choiceReports == 3) {
                                    System.out.println("============ TOP HOSTS BASED ON LISTING COUNT ===========");
                                    ReportManager.topHosts(conn);
                                } else if (choiceReports == 4) {
                                    System.out.println("============== LISTING PERCENTAGE PER HOST ==============");
                                    ReportManager.listingPercentage(conn);
                                } else if (choiceReports == 5) {
                                    System.out.println("=========== TOP RENTERS BASED ON BOOKING COUNT ==========");
                                    ReportManager.topRenters(conn, sc);
                                } else if (choiceReports == 6) {
                                    System.out.println("=========== TOP USERS WITH MOST CANCELLATIONS ===========");
                                    ReportManager.topCancellations(conn);
                                } else if (choiceReports == 7) {
                                    System.out.println("=========== POPULAR PHRASES/WORDS PER LISTING ===========");
                                    ReportManager.popularPhrases(conn);
                                }
                            } while (inputReports.compareTo("0") != 0);
                        }
                    } while (inputBnB.compareTo("0") != 0);
                } else if (choice == 2) {
                    DatabaseWorker.loadSampleData(conn);
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
}

package mybnb;

public class Menus {

    public static void initialMenu() {
        System.out.println("==================== MENU ====================");
        System.out.println("0. Exit.");
        System.out.println("1. Initialize Database. (First time setup)");
        System.out.println("2. Go to MyBnB.");
        System.out.print("Choose one of the previous options [0-2]: ");
    }

    public static void myBnBMenu() {
        System.out.println("==================== MYBNB ===================");
        System.out.println("0. Back.");
        System.out.println("1. Log in.");
        System.out.println("2. Create an account.");
        System.out.println("3. Search for listings.");
        System.out.println("4. View reports.");
        System.out.print("Choose one of the previous options [0-4]: ");
    }

    public static void renterMenu() {
        System.out.println("=================== RENTER ===================");
        System.out.println("0. Logout.");
        System.out.println("1. Book a listing.");
        System.out.println("2. Cancel a booking.");
        System.out.println("3. Review a host/listing.");
        System.out.println("4. Delete account.");
        System.out.print("Choose one of the previous options [0-4]: ");
    }

    public static void hostMenu() {
        System.out.println("==================== HOST ====================");
        System.out.println("0. Logout.");
        System.out.println("1. Create a listing.");
        System.out.println("2. Update a listing's price.");
        System.out.println("3. Update a listing's availability.");
        System.out.println("4. Add an extra availability date.");
        System.out.println("5. Remove a listing.");
        System.out.println("6. Cancel a booking.");
        System.out.println("7. Review a renter.");
        System.out.println("8. Delete account.");
        System.out.print("Choose one of the previous options [0-7]: ");
    }

    public static void queriesMenu() {
        System.out.println("=================== SEARCH ===================");
        System.out.println("0. Back.");
        System.out.println("1. Search by latitude/longitude.");
        System.out.println("2. Search by postal code.");
        System.out.println("3. Search by address.");
        System.out.println("4. Search by date range.");
        System.out.println("5. Search by multiple filters.");
        System.out.print("Choose one of the previous options [0-5]: ");
    }

    public static void reportsMenu() {
        System.out.println("================== REPORTS ===================");
        System.out.println("0. Back.");
        System.out.println("1. Total number of bookings.");
        System.out.println("2. Total number of listings.");
        System.out.println("3. Top hosts based on # of listings.");
        System.out.println("4. Listing percentage per host.");
        System.out.println("5. Top renters based on # of bookings.");
        System.out.println("6. Top users with the most cancellations.");
        System.out.println("7. Popular phrases/words per listing.");
        System.out.print("Choose one of the previous options [0-7]: ");
    }
}

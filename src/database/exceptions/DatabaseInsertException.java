package database.exceptions;

public class DatabaseInsertException extends Exception {

    public DatabaseInsertException() {
        super();
    }

    public DatabaseInsertException(String input) {
        super(input);
    }
}

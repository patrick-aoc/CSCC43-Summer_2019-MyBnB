package users;

import java.math.BigInteger;

public class Renter extends User {

    private String creditCardNumber;

    public Renter() {
        super();
    }

    public String getCreditCardNumber() { return this.creditCardNumber; }

    public void setCreditCardNumber(String ccardNum) { this.creditCardNumber = ccardNum;}
}

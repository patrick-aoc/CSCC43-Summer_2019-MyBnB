package users;

public class User {

    private String dateOfBirth;
    private String name;
    private String username;
    private int sin;
    private String occupation;
    private String address;
    private String password;

    public User() {
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public String getName() {
        return this.name;
    }

    public int getSin() {
        return this.sin;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public String getAddress() { return this.address; }

    public String getPassword() { return this.password; }

    public String getUsername() { return this.username; }

    public void setDateOfBirth(String dob) { this.dateOfBirth = dob; }

    public void setName(String name) { this.name = name; }

    public void setSin(int sin) { this.sin = sin; }

    public void setOccupation(String occupation) { this.occupation = occupation; }

    public void setAddress(String address) { this.address = address; }

    public void setPassword(String password) { this.password = password; }

    public void setUsername(String username) { this.username = username; }

}

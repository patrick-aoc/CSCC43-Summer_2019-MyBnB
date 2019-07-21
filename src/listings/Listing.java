package listings;

import java.math.BigDecimal;

public class Listing {

    private int host;
    private int renter;
    private String listingType;
    private int listingId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String postalCode;
    private String city;
    private String country;

    public Listing() {
    }

    public int getHost() { return this.host; }

    public int getRenter() { return this.renter; }

    public String getListingType() { return this.listingType; }

    public int getListingId() { return this.listingId; }

    public BigDecimal getLatitude() { return this.latitude; }

    public BigDecimal getLongitude() { return this.longitude; }

    public String getPostalCode() { return this.postalCode; }

    public String getCity() { return this.city; }

    public  String getCountry() { return this.country; }

    public void setHost(int host) { this.host = host; }

    public void setRenter(int renter) { this.renter = renter; }

    public void setListingType(String listingType) { this.listingType = listingType; }

    public void setListingId(int listingId) { this.listingId = listingId; }

    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public void setCity(String city) { this.city = city; }

    public void setCountry(String country) { this.country = country; }
}

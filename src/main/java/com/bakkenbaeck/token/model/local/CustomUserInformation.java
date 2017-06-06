package com.bakkenbaeck.token.model.local;


public class CustomUserInformation  {
    private String about;
    private String location;
    private String payment_address;

    public CustomUserInformation() {}

    public String getAbout() {
        return this.about;
    }

    public String getLocation() {
        return this.location;
    }

    public String getPaymentAddress() {
        return this.payment_address;
    }
}
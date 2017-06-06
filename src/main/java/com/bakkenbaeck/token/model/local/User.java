package com.bakkenbaeck.token.model.local;

public class User {

    private String owner_address;
    private String username;
    private CustomUserInformation custom;

    // ctors
    public User() {}

    /*
    private User(final Parcel in) {
        owner_address = in.readString();
        username = in.readString();
        custom = in.readParcelable(CustomUserInformation.class.getClassLoader());
    }
    */

    // Getters

    public String getUsername() {
        return username;
    }

    public String getOwnerAddress() {
        return owner_address;
    }

    public String getAbout() {
        return custom == null ? null : this.custom.getAbout();
    }

    public String getLocation() {
        return custom == null ? null : this.custom.getLocation();
    }

    public String getPaymentAddress() {
        return custom == null ? null : this.custom.getPaymentAddress();
    }



    // Setters

    public void setUsername(final String username) {
        this.username = username;
    }

}

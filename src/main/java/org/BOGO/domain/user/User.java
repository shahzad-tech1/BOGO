package org.BOGO.domain.user;

import org.BOGO.domain.common.PersonalDetails;


public abstract class User {

    private int userID;

    private PersonalDetails creds;

    // ---------- Constructors ----------
    protected User(int userID, String name, String email, String phoneNumber, String password) {
        this.userID = userID;
        this.creds = new PersonalDetails(userID, name, email, phoneNumber, password);
    }

    public int getUserID() { return userID; }
    public int getUserId() { return userID; }
    public PersonalDetails getCreds() { return creds; }
    public PersonalDetails getPersonalDetails() { return creds; }
    public String getName() { return creds == null ? null : creds.getName(); }
    public String getEmail() { return creds == null ? null : creds.getEmail(); }
    public String getPassword() { return creds == null ? null : creds.getPassword(); }
    public String getCNIC() { return creds == null ? null : creds.getCNIC(); }

    // ---------- Setters ----------
    public void setUserID(int userID) { this.userID = userID; }
    public void setCreds(PersonalDetails creds) { this.creds = creds; }

    // ---------- Business Methods ----------
    public boolean changePassword(String oldPassword, String newPassword) {
        if (creds.getPassword().equals(oldPassword)) {
            creds.setPassword(newPassword);
            return true;
        }
        return false;
    }
    public boolean validateCredentials() {
        return creds != null && creds.validate();
    }
    public void updateProfile(String name, String email, String phoneNumber) {
        if (name != null && !name.isBlank())        creds.setName(name);
        if (email != null && !email.isBlank())      creds.setEmail(email);
        if (phoneNumber != null && !phoneNumber.isBlank()) creds.setPhoneNumber(phoneNumber);
    }
}

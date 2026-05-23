package org.BOGO.domain.common;

public class PersonalDetails {
    private int pdId;
    private String name;
    private String email;
    private String password;
    private String cnic;

    public PersonalDetails() {
    }

    public PersonalDetails(int pdId, String name, String email, String cnic, String password) {
        this.pdId = pdId;
        this.name = name;
        this.email = email;
        this.cnic = cnic;
        this.password = password;
    }

    public int getPdId() {
        return pdId;
    }

    public int getUserId() {
        return pdId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCNIC() {
        return cnic;
    }

    public String getCnic() {
        return cnic;
    }

    public String getPhoneNumber() {
        return cnic;
    }

    public void setPdId(int pdId) {
        this.pdId = pdId;
    }

    public void setUserId(int userId) {
        this.pdId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCNIC(String cnic) {
        this.cnic = cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.cnic = phoneNumber;
    }

    public boolean validate() {
        return name != null && !name.isBlank()
                && email != null && email.contains("@")
                && password != null && !password.isBlank();
    }
}

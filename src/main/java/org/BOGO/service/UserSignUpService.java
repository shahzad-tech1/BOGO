package org.BOGO.service;

import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.user.User;

/**
 * UserSignUpService handles passenger self-registration.
 * Delegates to AuthService to avoid duplication.
 */
public class UserSignUpService {

    private final AuthService authService = new AuthService();

    /**
     * Registers a new passenger account.
     *
     * @param name     Full name
     * @param email    Email address (must be unique)
     * @param password Plain-text password
     * @param cnic     CNIC number
     * @return The created User on success, null on failure
     */
    public User registerPassenger(String name, String email, String password, String cnic) {
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || cnic == null || cnic.isBlank()) {
            System.out.println("[UserSignUpService] registerPassenger: all fields are required.");
            return null;
        }

        PersonalDetails pd = new PersonalDetails(0, name, email.trim().toLowerCase(), cnic, password);
        User user = authService.register(pd, "PASSENGER");

        if (user == null) {
            System.out.println("[UserSignUpService] registerPassenger: registration failed for " + email);
        } else {
            System.out.println("[UserSignUpService] registerPassenger: success userId=" + user.getUserID());
        }
        return user;
    }
}

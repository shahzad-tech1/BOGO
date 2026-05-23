package org.BOGO.controller;

import org.BOGO.service.AuthService;
import org.BOGO.service.UserSignUpService;
import org.BOGO.domain.common.PersonalDetails;
import org.BOGO.domain.user.User;

public class UserController {

    private final AuthService     authService = new AuthService();
    private final UserSignUpService signUpService = new UserSignUpService();

    public User login(String email, String password) {
        return authService.authenticate(email, password);
    }

    public User registerPassenger(String name, String email, String password, String cnic) {
        return authService.register(new PersonalDetails(0, name, email, cnic, password), "PASSENGER");
    }

    public void logout(User user) {
        if (user != null) {
            authService.logout(user.getUserID());
        }
    }
}

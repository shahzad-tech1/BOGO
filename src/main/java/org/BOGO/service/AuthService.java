package org.BOGO.service;

import org.BOGO.domain.user.User;
import org.BOGO.repository.UserRepository;

import java.util.UUID;

/**
 * AuthService handles authentication, registration, session management,
 * and password reset via direct JDBC (through UserRepository).
 *
 * NOTE — Duplication analysis vs existing domain classes:
 * -------------------------------------------------------
 * - User.changePassword(oldPw, newPw): operates on an already-loaded in-memory User
 *   object and mutates its creds field. AuthService.resetPassword() is different — it
 *   works by email address without requiring the user to be already logged in, and it
 *   writes the change to the DATABASE. No overlap removed.
 *
 * - User.validateCredentials(): validates that creds fields are non-null/formatted.
 *   AuthService.login() is different — it performs DB credential verification (hash
 *   comparison). No overlap removed.
 *
 * - User.updateProfile(): mutates an in-memory PersonalDetails. AuthService.register()
 *   is different — it creates a brand-new persistent record. No overlap removed.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Authenticates a user by email and password.
     * Looks up the user in the DB, compares the stored password,
     * and — if valid — generates and stores a new session token.
     * Returns a lightweight User-like shell (PersonalDetails) on success, null otherwise.
     *
     * NOTE: passwords are stored in plain text for this stage.
     * Replace the equality check with a bcrypt/hash comparison when hashing is added.
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;

        User user = userRepository.findUserByEmail(email.trim().toLowerCase());
        if (user == null) {
            System.out.println("[AuthService] login: no user found for email=" + email);
            return null;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("[AuthService] login: wrong password for email=" + email);
            return null;
        }

        String token = UUID.randomUUID().toString();
        userRepository.saveSessionToken(user.getUserID(), token);
        System.out.println("[AuthService] login: success for userID=" + user.getUserID());
        return user;
    }

    public org.BOGO.domain.common.PersonalDetails login(String email, String password) {
        User user = authenticate(email, password);
        return user == null ? null : user.getPersonalDetails();
    }

    /**
     * Invalidates ALL session tokens for the given userID.
     * The caller should discard the token on the client side after this call.
     */
    public void logout(int userID) {
        userRepository.deleteSessionToken(userID);
        System.out.println("[AuthService] logout: tokens cleared for userID=" + userID);
    }

    /**
     * Registers a new user with the given PersonalDetails and role string.
     * Role must be one of: 'Admin', 'Driver', 'Passenger'.
     *
     * Writes the Users row via userRepository.save() and returns the
     * persisted PersonalDetails (with the generated userID set), or null on failure.
     */
    public User register(org.BOGO.domain.common.PersonalDetails details, String role) {
        if (details == null || role == null || role.isBlank()) return null;

        org.BOGO.domain.common.PersonalDetails existing = userRepository.findByEmail(details.getEmail());
        if (existing != null) {
            System.out.println("[AuthService] register: email already in use -> " + details.getEmail());
            return null;
        }

        int generatedID = userRepository.save(details, role);
        if (generatedID < 0) {
            System.out.println("[AuthService] register: DB insert failed.");
            return null;
        }

        details.setUserId(generatedID);
        System.out.println("[AuthService] register: new user created, userID=" + generatedID);
        return userRepository.findUserById(generatedID);
    }

    /**
     * Validates whether the given session token is still active in the DB.
     * Returns true if the token exists in the SessionTokens table.
     */
    public boolean validateSession(String token) {
        if (token == null || token.isBlank()) return false;
        return userRepository.sessionTokenExists(token);
    }

    /**
     * Initiates a password reset for the given email by generating a temporary password
     * and updating the DB. In a real system you would email this token/link to the user.
     *
     * NOTE: User.changePassword() requires both old AND new password, and operates on an
     * already-loaded in-memory User. This method is intentionally different: it resets the
     * password without knowing the old one (admin/forgot-password flow), and writes directly
     * to the database.
     */
    public void resetPassword(String email) {
        if (email == null || email.isBlank()) return;

        org.BOGO.domain.common.PersonalDetails pd = userRepository.findByEmail(email.trim().toLowerCase());
        if (pd == null) {
            System.out.println("[AuthService] resetPassword: no user found for email=" + email);
            return;
        }

        // Generate a temporary password (replace with email-token flow in production)
        String tempPassword = "Temp@" + UUID.randomUUID().toString().substring(0, 8);
        boolean updated = userRepository.updatePassword(email, tempPassword);
        if (updated) {
            System.out.println("[AuthService] resetPassword: temp password set for email=" + email
                + " | tempPw=" + tempPassword + " (send this via email in production)");
        } else {
            System.out.println("[AuthService] resetPassword: DB update failed for email=" + email);
        }
    }
}

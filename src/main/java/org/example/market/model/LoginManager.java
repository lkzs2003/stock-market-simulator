package org.example.market.model;

public class LoginManager {
    private final UserManager userManager;
    private User loggedInUser;

    public LoginManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean login(String username, String password) {
        if (userManager.validatePassword(username, password)) {
            loggedInUser = userManager.getUser(username);
            System.out.println("User " + username + " logged in successfully.");
            return true;
        } else {
            System.out.println("Invalid username or password.");
            return false;
        }
    }

    public void logout() {
        if (loggedInUser != null) {
            System.out.println("User " + loggedInUser.getUsername() + " logged out.");
            loggedInUser = null;
        }
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}

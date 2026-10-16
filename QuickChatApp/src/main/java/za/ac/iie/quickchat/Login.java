package za.ac.iie.quickchat;

/**
 * Stub Login class — replace with your full Part 1/2 implementation.
 */
public class Login {

    private String username;
    private String password;
    private String firstName;
    private String lastName;

    public boolean checkUserName(String username) {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    public String getUsernameValidationMessage(String username) {
        if (checkUserName(username)) return "Username successfully captured.";
        return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
    }

    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper   = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit   = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(c -> "!@#$%^&*".indexOf(c) >= 0);
        return hasUpper && hasDigit && hasSpecial;
    }

    public String getPasswordValidationMessage(String password) {
        if (checkPasswordComplexity(password)) return "Password successfully captured.";
        return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
    }

    public boolean checkCellPhoneNumber(String cell) {
        return cell != null && cell.startsWith("+") && cell.substring(1).matches("\\d{10,}");
    }

    public String getCellValidationMessage(String cell) {
        if (checkCellPhoneNumber(cell)) return "Cell phone number successfully captured.";
        return "Cell phone number incorrectly formatted.";
    }

    public String registerUser(String username, String password, String cell,
                               String firstName, String lastName) {
        this.username  = username;
        this.password  = password;
        this.firstName = firstName;
        this.lastName  = lastName;
        return "User registered successfully.";
    }

    public boolean loginUser(String username, String password) {
        return this.username != null
                && this.username.equals(username)
                && this.password != null
                && this.password.equals(password);
    }

    public String returnLoginStatus(boolean loggedIn) {
        if (loggedIn) return "Welcome back " + firstName + " " + lastName + ", it is great to see you again.";
        return "Username or password incorrect, please try again.";
    }
}

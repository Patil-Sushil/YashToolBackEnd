package com.example.YashToolBackEnd.common.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final String PASSWORD_PATTERN =
        "^(?=.*[0-9])" +                    // at least one digit
        "(?=.*[a-z])" +                     // at least one lowercase letter
        "(?=.*[A-Z])" +                     // at least one uppercase letter
        "(?=.*[@#$%^&+=])" +                // at least one special character
        "(?=\\S+$)" +                       // no whitespace
        ".{8,}$";                           // at least 8 characters

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public static String getPasswordRequirements() {
        return "Password must be at least 8 characters long, contain at least one uppercase letter, " +
               "one lowercase letter, one digit, and one special character (@#$%^&+=)";
    }
}


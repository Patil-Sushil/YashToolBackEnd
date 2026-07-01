package com.kalibyte.YashTools.email.util;

import com.kalibyte.YashTools.email.exception.EmailException;
import java.util.regex.Pattern;

public final class EmailAddressValidator {
    private static final Pattern P = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@" +
                    "(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$");

    private EmailAddressValidator() {}

    public static boolean isValid(String email) {
        return email != null && P.matcher(email.trim()).matches();
    }

    public static void requireValid(String email) {
        if (!isValid(email)) throw new EmailException("Invalid email: " + email);
    }
}
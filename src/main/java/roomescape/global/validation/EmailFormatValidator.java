package roomescape.global.validation;

import java.util.regex.Pattern;
import roomescape.global.exception.InvalidEmailFormatException;

public final class EmailFormatValidator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailFormatValidator() {
    }

    public static void validate(String email) {
        if (email == null || email.isBlank() || !EMAIL.matcher(email).matches()) {
            throw new InvalidEmailFormatException();
        }
    }
}

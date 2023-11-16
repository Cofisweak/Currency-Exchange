package org.cofisweak.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.exception.MissingFieldException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorManager {
    public static void checkIsFieldFilled(String field, String fieldName) throws MissingFieldException {
        if (field == null || field.trim().isEmpty()) {
            throw new MissingFieldException(fieldName);
        }
    }

    public static boolean isValidCurrencyCode(String code) {
        code = code.trim();
        if (code.length() != 3) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }
}

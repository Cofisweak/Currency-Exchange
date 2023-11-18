package org.cofisweak.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.cofisweak.exception.IllegalRateException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static final String GET_LAST_INSERT_ROWID_SQL = "SELECT last_insert_rowid()";
    public static String extractCurrencyCodeFromUri(String uri) {
        return uri
                .substring(uri.lastIndexOf("/") + 1)
                .trim();
    }
    public static boolean isFieldEmpty(String field) {
        return field == null || field.trim().isEmpty();
    }
    public static boolean isInvalidCurrencyCode(String code) {
        if(code == null) return true;
        return !code.matches("\\w{3}");
    }

    public static boolean isValidCurrencyCodePair(String codePair) {
        if(codePair == null) return false;
        return codePair.matches("\\w{6}");
    }

    public static BigDecimal parseRate(String rateString) throws IllegalRateException {
        BigDecimal rate;
        try {
            rate = new BigDecimal(rateString);
        } catch (NumberFormatException e) {
            throw new IllegalRateException("Illegal number");
        }
        if(rate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalRateException("Number must be not negative");
        }
        return rate.setScale(6, RoundingMode.DOWN);
    }

    public static void processException(Exception e, int status, HttpServletResponse response) throws IOException {
        processException(e.getMessage(), status, response);
    }

    public static void processException(String errorMessage, int status, HttpServletResponse response) throws IOException {
        response.setStatus(status);
        ResponseBuilder.writeErrorToResponse(errorMessage, response);
    }
}

package org.cofisweak.exception;

public class InvalidCurrencyCodeException extends Exception {
    public InvalidCurrencyCodeException() {
        super("Invalid currency code. The currency code should be like 'USD' (3 letters)");
    }
}

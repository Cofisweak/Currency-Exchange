package org.cofisweak.exception;

public class InvalidCurrencyCodePairException extends Exception {
    public InvalidCurrencyCodePairException() {
        super("Invalid currency code pair");
    }
}

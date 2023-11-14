package org.cofisweak.exception;

public class CurrencyNotFoundException extends Exception {
    public CurrencyNotFoundException() {
        super("Currency not found");
    }
}

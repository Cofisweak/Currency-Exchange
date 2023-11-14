package org.cofisweak.exception;

public class CurrencyAlreadyExistsException extends Exception {
    public CurrencyAlreadyExistsException() {
        super("Currency already exists");
    }
}

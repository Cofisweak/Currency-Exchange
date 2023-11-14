package org.cofisweak.exception;

public class ExchangeRateAlreadyExistsException extends Exception {
    public ExchangeRateAlreadyExistsException() {
        super("Exchange rate already exists");
    }
}

package org.cofisweak.exception;

public class ExchangeRateNotFoundException extends Exception {
    public ExchangeRateNotFoundException() {
        super("Exchange rate not found");
    }
}

package org.cofisweak.exception;

public class MissingFieldException extends Exception {
    public MissingFieldException(String fieldName) {
        super("Missing required field \"" + fieldName + "\"");
    }
}

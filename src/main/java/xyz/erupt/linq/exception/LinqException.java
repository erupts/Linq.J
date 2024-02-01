package xyz.erupt.linq.exception;

public class LinqException extends RuntimeException {

    public LinqException(String message) {
        super(message);
    }

    public LinqException(Throwable cause) {
        super(cause);
    }
}

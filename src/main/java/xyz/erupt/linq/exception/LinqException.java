package xyz.erupt.linq.exception;

public class LinqException extends RuntimeException {

    public LinqException(String message) {
        super(message);
    }

    public LinqException(Throwable cause) {
        super(cause);
    }

    public LinqException(String message, Throwable e) {
        super(message, e);
    }
}

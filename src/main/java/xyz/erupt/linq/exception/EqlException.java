package xyz.erupt.linq.exception;

public class EqlException extends RuntimeException {

    public EqlException(String message) {
        super(message);
    }

    public EqlException(Throwable cause) {
        super(cause);
    }
}

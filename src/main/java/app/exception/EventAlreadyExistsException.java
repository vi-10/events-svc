package app.exception;

public class EventAlreadyExistsException extends RuntimeException{
    public EventAlreadyExistsException(String message) {
        super(message);
    }
}

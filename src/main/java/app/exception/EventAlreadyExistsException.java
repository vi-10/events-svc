package app.exception;

import org.springframework.http.HttpStatus;

public class EventAlreadyExistsException extends ApiException{

    public EventAlreadyExistsException(String title) {
        super(
                "An event with title " + title + " already exists.",
                "409",
                "Event Already Exists"
        );
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}

package app.exception;

import org.springframework.http.HttpStatus;

public class EventNotFoundException extends ApiException{


    public EventNotFoundException() {
        super(
                "The requested event could not be found.",
                "404",
                "Event Not Found"
        );
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

package app.exception;

import org.springframework.http.HttpStatus;

public class InvalidEventException extends ApiException{

    public InvalidEventException(String message) {
        super(
                message,
                "400",
                "Invalid Event"
        );
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

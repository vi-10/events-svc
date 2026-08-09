package app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException{
    private final String errorCode;
    private final String errorTitle;

    public ApiException(String message, String errorCode, String errorTitle) {

        super(message);
        this.errorCode = errorCode;
        this.errorTitle = errorTitle;
    }

    public abstract HttpStatus getHttpStatus();
}

package app.web.exception;

import app.exception.ApiException;
import app.web.dto.ErrorResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleApiException(ApiException exception) {

        log.error( "API exception occurred: {}", exception.getMessage(), exception);

        HttpStatus status = exception.getHttpStatus();

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(exception.getErrorCode())
                .errorTitle(exception.getErrorTitle())
                .message(exception.getMessage())
                .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException exception) {

        log.error("Validation error occurred: {}", exception.getMessage(), exception);

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request.");

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorCode("400")
                .errorTitle("Validation Error")
                .message(message)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception exception) {

        log.error( "Unexpected exception occurred: {}", exception.getMessage(), exception );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(500)
                        .error("Internal Server Error")
                        .errorCode("500")
                        .errorTitle("Internal Server Error")
                        .message("An unexpected error occurred.")
                        .build());
    }
}

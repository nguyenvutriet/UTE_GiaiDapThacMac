package nvt.vn.ute_forum.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;

@ControllerAdvice
public class MyHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        ErrorResponse errorResponse = new ErrorResponse("413", "Kich thuoc tep tai len vuot gioi han cho phep. " + (detail == null ? "" : detail));
        errorResponse.setTimeStamp(Instant.now().toEpochMilli());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception ex){
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value()+"", ex.getMessage());
        errorResponse.setTimeStamp(Instant.now().toEpochMilli());
        return ResponseEntity.badRequest().body(errorResponse);
    }

}

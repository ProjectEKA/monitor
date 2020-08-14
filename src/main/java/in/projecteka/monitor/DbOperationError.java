package in.projecteka.monitor;

import in.projecteka.monitor.model.Error;
import in.projecteka.monitor.model.ErrorRepresentation;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import static in.projecteka.monitor.model.ErrorCode.DB_OPERATION_FAILED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@ToString
public class DbOperationError extends Throwable {
    private final HttpStatus httpStatus;
    private final ErrorRepresentation error;
    private static final String ERROR_MESSAGE = "Failed to persist in database";

    public DbOperationError() {
        this.httpStatus = INTERNAL_SERVER_ERROR;
        this.error = new ErrorRepresentation(new Error(DB_OPERATION_FAILED, ERROR_MESSAGE));
    }
}

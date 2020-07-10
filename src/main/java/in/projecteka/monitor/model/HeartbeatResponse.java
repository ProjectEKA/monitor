package in.projecteka.monitor.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class HeartbeatResponse {
    private LocalDateTime timeStamp;
    private Status status;
    private Error error;
}

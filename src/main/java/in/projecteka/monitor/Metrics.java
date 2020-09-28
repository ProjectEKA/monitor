package in.projecteka.monitor;

import in.projecteka.monitor.model.Status;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Builder
@Value
public class Metrics {
    String bridgeId;
    String name;
    String type;
    String path;
    Status status;
    LocalDateTime lastUpTime;
    LocalDateTime lastCheckTime;
}

package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.ServiceProperties;
import in.projecteka.monitor.model.Status;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
public class FetchMetricTask {
    private final MetricRepository metricRepository;
    private final MetricServiceClient metricServiceClient;

    public void fetchMetricAndSave(ServiceProperties property){
        String path = String.format("%s%s", property.getUrl(), Constants.PATH_HEARTBEAT);
        HeartbeatResponse heartbeatResponse = metricServiceClient.getHeartbeat(path);
        if (heartbeatResponse != null && heartbeatResponse.getStatus().equals(Status.UP)){
            metricRepository.addMetric(property.getId(), property.getName(), property.getType(), path, Status.UP.name(), LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));
        }
        else {
            metricRepository.addMetric(property.getId(), property.getName(), property.getType(), path, Status.DOWN.name(), null, LocalDateTime.now(ZoneOffset.UTC));
        }
    }
}

package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Service;
import in.projecteka.monitor.model.ServiceProperties;
import in.projecteka.monitor.model.Status;
import io.prometheus.client.Gauge;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class Metric {
    private static final Gauge status = Gauge.build()
            .labelNames("Name", "Id", "Path", "Status", "LastUpTime")
            .name("Projecteka_metrics")
            .help("Heartbeat Status")
            .register();
    private final MetricRepository metricRepository;
    private final MetricServiceClient metricServiceClient;

    public void processRequests() {
        Service service = metricServiceClient.getService();
        process(service.getBridgeProperties());
        process(service.getConsentManagerProperties());
    }

    private void process(List<ServiceProperties> properties) {
        properties.forEach(property -> {
            String path = String.format("%s%s", property.getUrl(), Constants.PATH_HEARTBEAT);
            HeartbeatResponse heartbeatResponse = metricServiceClient.getHeartbeat(path);
            if (heartbeatResponse.getStatus().equals(Status.DOWN)) {
                String lastUpTime = metricRepository.getIfPresent(path).block();
                lastUpTime = lastUpTime == null ? "" : lastUpTime;
                status.labels(property.getName(),
                        property.getId(),
                        path,
                        heartbeatResponse.getStatus().toString(),
                        lastUpTime).set(0);
            } else {
                LocalDateTime lastUpTime = LocalDateTime.now(ZoneOffset.UTC);
                Boolean isPresent = isEntryPresent(path).block();
                if (isPresent != null && isPresent) {
                    metricRepository.update(lastUpTime, path).block();
                } else {
                    metricRepository.insert(path, Status.UP.toString(), lastUpTime).block();
                }
                status.labels(property.getName(),
                        property.getId(),
                        path,
                        heartbeatResponse.getStatus().toString(),
                        lastUpTime.toString()).inc();
            }
        });
    }

    private Mono<Boolean> isEntryPresent(String path) {
        return metricRepository.getIfPresent(path)
                .map(entry -> !Objects.isNull(entry));
    }
}

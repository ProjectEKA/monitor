package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Status;
import io.prometheus.client.Gauge;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.resolve;

@AllArgsConstructor
public class Metric {
    private final WebClient webClient;
    private final MetricsRepository metricsRepository;
    static final Gauge status = Gauge.build()
            .labelNames("Path", "Status")
            .name("Projecteka_metrics")
            .help("Heartbeat Status")
            .register();

    public void processRequests() {
                getBridgeUrls()
                .forEach(url -> {
                    String path = String.format("%s/v1/heartbeat", url);
                    HeartbeatResponse heartbeatResponse = getHeartbeat(path);
                    status.labels(path, heartbeatResponse.getStatus().toString()).set(0);
                    if (heartbeatResponse.getStatus().equals(Status.UP)) {
                        status.labels(path, heartbeatResponse.getStatus().toString()).inc();
                    }
                });
    }

    private HeartbeatResponse getHeartbeat(String path) {
        return webClient
                .get()
                .uri(path)
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> Mono.error(new Throwable("Server error")))
                .bodyToMono(HeartbeatResponse.class)
                .block();
    }

    public List<String> getBridgeUrls() {
        return metricsRepository.selectPaths().collectList().block();
    }
}

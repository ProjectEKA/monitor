package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Status;
import io.prometheus.client.Gauge;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.OK;

@AllArgsConstructor
public class Metric {
    static final Gauge status = Gauge.build()
            .labelNames("Path", "Status", "LastUpTime")
            .name("Projecteka_metrics")
            .help("Heartbeat Status")
            .register();
    private final WebClient webClient;
    private final MetricsRepository metricsRepository;

    public void processRequests() {
        getBridgeUrls()
                .forEach(url -> {
                    String path = String.format("%s/v1/heartbeat", url);
                    HeartbeatResponse heartbeatResponse = getHeartbeat(path);
                    if (heartbeatResponse.getStatus().equals(Status.DOWN)) {
                        String lastUpTime = metricsRepository.getIfPresent(path).block();
                        status.labels(path, heartbeatResponse.getStatus().toString(), lastUpTime).set(0);
                    } else {
                        LocalDateTime lastUpTime = LocalDateTime.now(ZoneOffset.UTC);
                        Boolean isPresent = isEntryPresent(path).block();
                        if (isPresent != null && isPresent) {
                            metricsRepository.update(lastUpTime, path).block();
                        } else {
                            metricsRepository.insert(path, Status.UP.toString(), lastUpTime).block();
                        }
                        status.labels(path, heartbeatResponse.getStatus().toString(), lastUpTime.toString()).inc();
                    }
                });
    }

    private Mono<Boolean> isEntryPresent(String path) {
        return metricsRepository.getIfPresent(path)
                .map(entry -> !Objects.isNull(entry));
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
        //TODO
        //Get gateway url from config
        return webClient
                .get()
                .uri("http://localhost:8000/v1/getBridgeUrls")
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> Mono.error(new Throwable("Server error")))
                .bodyToFlux(String.class)
                .collectList().block();


    }
}

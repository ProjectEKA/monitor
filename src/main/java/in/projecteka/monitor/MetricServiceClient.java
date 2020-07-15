package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Service;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.OK;

@AllArgsConstructor
public class MetricServiceClient {
    private final WebClient webClient;
    private final GatewayProperties gatewayProperties;

    public HeartbeatResponse getHeartbeat(String path) {
        return webClient
                .get()
                .uri(path)
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> Mono.error(new Throwable("Server error")))
                .bodyToMono(HeartbeatResponse.class)
                .block();
    }

    public Service getService() {
        return webClient
                .get()
                .uri(String.format("%s/service-properties", gatewayProperties.getBaseUrl()))
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> Mono.error(new Throwable("Server error")))
                .bodyToMono(Service.class)
                .block();
    }
}

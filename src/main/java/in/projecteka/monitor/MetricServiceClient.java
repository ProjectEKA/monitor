package in.projecteka.monitor;

import in.projecteka.monitor.model.Error;
import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Service;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static in.projecteka.monitor.model.ErrorCode.UNKNOWN_ERROR_OCCURRED;
import static in.projecteka.monitor.model.Status.DOWN;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.error;

@AllArgsConstructor
public class MetricServiceClient {
    public static final String SERVER_ERROR = "Server error";
    private final WebClient webClient;
    private final GatewayProperties gatewayProperties;
    private static final Logger logger = LoggerFactory.getLogger(MetricServiceClient.class);

    public HeartbeatResponse getHeartbeat(String path) {
        return webClient
                .get()
                .uri(path)
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .doOnNext(logger::error)
                                .then(error(new Throwable(SERVER_ERROR))))
                .bodyToMono(HeartbeatResponse.class)
                .onErrorResume(throwable -> Mono.just(HeartbeatResponse.builder()
                        .status(DOWN)
                        .error(Error.builder().code(UNKNOWN_ERROR_OCCURRED).message(SERVER_ERROR).build())
                        .build()))
                .block();
    }

    public Service getService() {
        return webClient
                .get()
                .uri(format("%s/service-properties", gatewayProperties.getBaseUrl()))
                .retrieve()
                .onStatus(httpStatus -> httpStatus != OK,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .doOnNext(logger::error)
                                .then(error(new Throwable(SERVER_ERROR))))
                .bodyToMono(Service.class)
                .block();
    }
}

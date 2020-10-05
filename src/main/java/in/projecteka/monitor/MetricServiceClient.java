package in.projecteka.monitor;

import in.projecteka.monitor.model.Service;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;

@AllArgsConstructor
public class MetricServiceClient {
    public static final String SERVER_ERROR = "Server error";
    private final RestTemplate restTemplate;
    private final GatewayProperties gatewayProperties;
    private static final Logger logger = LoggerFactory.getLogger(MetricServiceClient.class);

    public boolean isBridgeAccessible(String path) {
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(path, String.class);
        } catch (Exception e) {
            logger.error(String.format("Error on path %s:", path), e);
            return false;
        }
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.error(String.format("Unexpected response from path %s: %s", path, responseEntity.getBody()));
            return false;
        }
        return true;
    }

    public Service getService() {
        String url = format("%s/service-properties", gatewayProperties.getBaseUrl());
        ResponseEntity<Service> responseEntity = restTemplate.getForEntity(url, Service.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        logger.error(String.format("Error while fetching service properties: %s", responseEntity.getBody()));
        throw new RuntimeException(SERVER_ERROR);
    }
}

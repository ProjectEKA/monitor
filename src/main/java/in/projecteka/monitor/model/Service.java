package in.projecteka.monitor.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class Service {
    private List<ServiceProperties> bridgeProperties;
    private List<ServiceProperties> consentManagerProperties;
}

package in.projecteka.monitor.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ServiceProperties {
    private String name;
    private String url;
    private String id;
}

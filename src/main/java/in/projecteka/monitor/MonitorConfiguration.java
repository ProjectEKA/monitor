package in.projecteka.monitor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MonitorConfiguration {

    @Bean
    public Metric metric(WebClient.Builder builder){
        return new Metric(builder.build());
    }
}

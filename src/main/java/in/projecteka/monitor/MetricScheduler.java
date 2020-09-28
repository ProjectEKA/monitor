package in.projecteka.monitor;

import in.projecteka.monitor.model.HeartbeatResponse;
import in.projecteka.monitor.model.Service;
import in.projecteka.monitor.model.ServiceProperties;
import in.projecteka.monitor.model.Status;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class MetricScheduler {
    private TaskExecutor executor;
    private final MetricRepository metricRepository;
    private final MetricServiceClient metricServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(MetricScheduler.class);
    
    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void fetchMetrics(){
        logger.info("Starting to fetch metrics");
        Service service = metricServiceClient.getService();

        process(service.getBridgeProperties());
        process(service.getConsentManagerProperties());
    }

    private void process(List<ServiceProperties> properties) {
        properties.forEach(property -> executor.execute(
                () -> new FetchMetricTask(metricRepository, metricServiceClient).fetchMetricAndSave(property))
        );
    }

}

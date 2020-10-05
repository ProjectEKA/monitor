package in.projecteka.monitor;

import in.projecteka.monitor.model.Service;
import in.projecteka.monitor.model.ServiceProperties;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@AllArgsConstructor
public class MetricScheduler {
    private final TaskExecutor executor;
    private final MetricRepository metricRepository;
    private final MetricServiceClient metricServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(MetricScheduler.class);

    @Scheduled(fixedDelayString = "${monitor.scheduler.interval}", initialDelayString = "${monitor.scheduler.initialDelay}")
    public void fetchMetrics() {
        logger.info("Starting to fetch metrics");
        Service service = metricServiceClient.getService();

        process(service.getBridgeProperties());
        process(service.getConsentManagerProperties());
    }

    private void process(List<ServiceProperties> properties) {
        properties.forEach(property -> executor.execute(
                () -> {
                    try {
                        new FetchMetricTask(metricRepository, metricServiceClient).fetchMetricAndSave(property);
                    } catch (Exception e) {
                        logger.error(String.format("Error while fetching metric for bridge %s with id %s from path %s", property.getName(), property.getId(), property.getUrl()), e);
                    }
                })
        );
    }

}

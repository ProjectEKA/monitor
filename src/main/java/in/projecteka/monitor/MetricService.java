package in.projecteka.monitor;

import io.prometheus.client.Gauge;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;

import static in.projecteka.monitor.model.Status.DOWN;

@AllArgsConstructor
public class MetricService {
    public static final String PROJECT_EKA_METRICS = "Projecteka_metrics_";
    private final MetricRepository metricRepository;
    private static final HashMap<String, Gauge> gaugeMap = new HashMap<>();

    public void processRequests() {
        List<Metrics> allMetrics = metricRepository.findAllMetrics();
        for (Metrics metrics : allMetrics) {
            if (!gaugeMap.containsKey(PROJECT_EKA_METRICS + metrics.getType())) {
                Gauge gaugeStatus = Gauge.build()
                        .labelNames("Name", "Id", "Path", "Status", "LastUpTime", "LastCheckTime")
                        .name(PROJECT_EKA_METRICS + metrics.getType())
                        .help("Heartbeat Status")
                        .register();

                gaugeMap.put(PROJECT_EKA_METRICS + metrics.getType(), gaugeStatus);
                Gauge.Child child = gaugeStatus.labels(metrics.getName(),
                        metrics.getBridgeId(),
                        metrics.getPath(),
                        metrics.getStatus().name(),
                        String.valueOf(metrics.getLastUpTime()),
                        String.valueOf(metrics.getLastCheckTime()));

                //What does the below do exactly?
                if (DOWN.equals(metrics.getStatus())) {
                    child.set(0);
                } else {
                    child.inc();
                }
            }
        }
    }
}

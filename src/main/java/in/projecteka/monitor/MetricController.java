package in.projecteka.monitor;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;


@RestController
@AllArgsConstructor
public class MetricController {
    private Metric metric;

    @GetMapping(path = "/metrics")
    public void metrics(Writer responseWriter) throws IOException {
        metric.processRequests();
        TextFormat.write004(responseWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
        responseWriter.close();
    }
}

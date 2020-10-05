package in.projecteka.monitor;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Writer;


@RestController
@AllArgsConstructor
public class MetricController {
    private final MetricService metricService;

    @GetMapping(path = "/metrics")
    public void metrics(Writer responseWriter) throws IOException {
        metricService.processRequests();
        TextFormat.write004(responseWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
        responseWriter.close();
    }
}

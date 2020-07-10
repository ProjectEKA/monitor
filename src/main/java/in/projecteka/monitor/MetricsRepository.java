package in.projecteka.monitor;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public class MetricsRepository {
    private final PgPool dbClient;

    public MetricsRepository(PgPool dbClient) {
        this.dbClient = dbClient;
    }

    private static final String INSERT_TO_METRICS = "INSERT INTO metrics " +
            "(path, status, last_up_time) VALUES ($1, $2, $3)";

    public Mono<Void> insert(String path, String status, LocalDateTime lastUpTime) {
        return Mono.create(monoSink ->
                dbClient.preparedQuery(INSERT_TO_METRICS)
                        .execute(Tuple.of(path, status, lastUpTime),
                                handler -> {
                                    if (handler.failed()) {
                                        monoSink.error(new DbOperationError());
                                        return;
                                    }
                                    monoSink.success();
                                }));
    }

}

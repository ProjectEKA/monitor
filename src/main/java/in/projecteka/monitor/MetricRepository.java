package in.projecteka.monitor;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class MetricRepository {
    private static final String INSERT_TO_METRICS = "INSERT INTO metrics " +
            "(path, status, last_up_time) VALUES ($1, $2, $3)";
    private static final String SELECT_TO_METRICS = "SELECT path FROM metrics";
    private static final String UPDATE_TO_METRICS = "UPDATE metrics SET last_up_time = $1 WHERE path = $2";
    private static final String SELECT_LAST_UP_TIME = "SELECT last_up_time FROM metrics WHERE path=$1";
    private final PgPool dbClient;

    public MetricRepository(PgPool dbClient) {
        this.dbClient = dbClient;
    }

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

    public Mono<Void> update(LocalDateTime lastUpTime, String path) {
        return Mono.create(monoSink ->
                dbClient.preparedQuery(UPDATE_TO_METRICS)
                        .execute(Tuple.of(lastUpTime, path),
                                handler -> {
                                    if (handler.failed()) {
                                        monoSink.error(new DbOperationError());
                                        return;
                                    }
                                    monoSink.success();
                                }));
    }

    public Mono<String> getIfPresent(String path) {
        return Mono.create(monoSink ->
                dbClient.preparedQuery(SELECT_LAST_UP_TIME)
                        .execute(Tuple.of(path),
                                handler -> {
                                    if (handler.failed()) {
                                        monoSink.error(new DbOperationError());
                                        return;
                                    }
                                    var iterator = handler.result().iterator();
                                    if (!iterator.hasNext()) {
                                        monoSink.success();
                                        return;
                                    }
                                    monoSink.success(iterator.next().getValue("last_up_time").toString());
                                }));
    }
}

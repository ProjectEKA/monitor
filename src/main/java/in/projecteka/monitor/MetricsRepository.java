package in.projecteka.monitor;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class MetricsRepository {
    private final PgPool dbClient;
    private final static Logger logger = LoggerFactory.getLogger(MetricsRepository.class);
    public MetricsRepository(PgPool dbClient) {
        this.dbClient = dbClient;
    }

    private static final String INSERT_TO_METRICS = "INSERT INTO metrics " +
            "(path, status, last_up_time) VALUES ($1, $2, $3)";

    private static final String SELECT_TO_METRICS = "SELECT path FROM metrics";

    private static final String UPDATE_TO_METRICS = "UPADTE metrics SET status = $1 , lastUpTime = $2 WHERE path = $3";

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

    public Mono<Void> update(String status, LocalDateTime lastUpTime,String path) {
        return Mono.create(monoSink ->
                dbClient.preparedQuery(UPDATE_TO_METRICS)
                        .execute(Tuple.of(status, lastUpTime,path),
                                handler -> {
                                    if (handler.failed()) {
                                        monoSink.error(new DbOperationError());
                                        return;
                                    }
                                    monoSink.success();
                                }));
    }

    public Flux<String> selectPaths() {
        return Flux.create(fluxSink -> dbClient.preparedQuery(SELECT_TO_METRICS)
                .execute(
                        handler -> {
                            if (handler.failed()) {
                                logger.error(handler.cause().getMessage(), handler.cause());
                                fluxSink.error(new DbOperationError());
                            } else {
                                StreamSupport.stream(handler.result().spliterator(), false)
                                        .map(row -> row.getString("path"))
                                        .forEach(fluxSink::next);
                                fluxSink.complete();
                            }
                        }));
    }
}

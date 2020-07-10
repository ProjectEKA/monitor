package in.projecteka.monitor;

import in.projecteka.monitor.model.DbOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MonitorConfiguration {

    @Bean
    public Metric metric(WebClient.Builder builder) {
        return new Metric(builder.build());
    }

    @Bean
    public PgPool pgPool(DbOptions dbOptions) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(dbOptions.getPort())
                .setHost(dbOptions.getHost())
                .setDatabase(dbOptions.getSchema())
                .setUser(dbOptions.getUser())
                .setPassword(dbOptions.getPassword());

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(dbOptions.getPoolSize());

        return PgPool.pool(connectOptions, poolOptions);
    }

    @Bean
    public MetricsRepository metricsRepository(PgPool pgPool) {
        return new MetricsRepository(pgPool);
    }
}

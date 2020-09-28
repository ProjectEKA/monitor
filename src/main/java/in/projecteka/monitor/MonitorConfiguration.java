package in.projecteka.monitor;

import in.projecteka.monitor.model.DbOptions;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;

@Configuration
public class MonitorConfiguration {

    @Bean
    public MetricService metric(MetricRepository metricRepository,
                                MetricServiceClient metricServiceClient) {
        return new MetricService(metricRepository, metricServiceClient);
    }

    @Bean
    public MetricServiceClient metricServiceClient(WebClient.Builder builder, GatewayProperties gatewayProperties){
        return new MetricServiceClient(builder.build(), gatewayProperties);
    }

    @Bean
    public DataSource dataSource(DbOptions dbOptions) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(buildUrl(dbOptions));
        dataSource.setUsername(dbOptions.getUser());
        dataSource.setPassword(dbOptions.getPassword());

        return dataSource;
    }
    private String buildUrl(DbOptions dbOptions) {
        return String.format("jdbc:postgresql://%s:%s/%s", dbOptions.getHost(), dbOptions.getPort(), dbOptions.getSchema());
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public MetricRepository metricRepository(JdbcTemplate jdbcTemplate){
      return new MetricRepository(jdbcTemplate);
    }

    @Bean("MetricTaskExecutor")
    public TaskExecutor taskExecutor () {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(100);
        taskExecutor.setQueueCapacity(50);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        taskExecutor.setKeepAliveSeconds(120);
        return taskExecutor;
    }

    @Bean
    public MetricScheduler metricScheduler(@Qualifier("MetricTaskExecutor") TaskExecutor taskExecutor,
                                           MetricRepository metricRepository,
                                           MetricServiceClient metricServiceClient){
        return new MetricScheduler(taskExecutor, metricRepository, metricServiceClient);
    };

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
    public ReactiveMetricRepository metricsRepository(PgPool pgPool) {
        return new ReactiveMetricRepository(pgPool);
    }
}

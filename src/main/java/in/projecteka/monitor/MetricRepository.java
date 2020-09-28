package in.projecteka.monitor;

import in.projecteka.monitor.model.Status;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
public class MetricRepository {
    private JdbcTemplate jdbcTemplate;

    public void addMetric(String bridgeId, String name, String type, String path, String status, LocalDateTime lastUpTime, LocalDateTime lastCheckTime) {
        insertOrUpdateMetric(bridgeId, name, type, path, status, lastUpTime, lastCheckTime);
        insertMetricHistory(bridgeId, name, type, path, status, lastCheckTime);
    }

    private void insertMetricHistory(String bridgeId, String name, String type, String path, String status, LocalDateTime dateCreated) {
        String sql = "INSERT INTO metrics_history(bridge_id, name, type, path, status, date_created) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, bridgeId, name, type, path, status, dateCreated);
    }

    private void insertOrUpdateMetric(String bridgeId, String name, String type, String path, String status, LocalDateTime lastUpTime, LocalDateTime lastCheckTime) {
        if (!exist(path)) {
            String sql = "INSERT INTO metrics(bridge_id, name, type, path, status, last_up_time, last_check_time) values (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, bridgeId, name, type, path, status, lastUpTime, lastCheckTime);
        } else {
            String updateMetricsQuery = "UPDATE metrics SET status = " + status + ",";
            if (lastUpTime != null) {
                updateMetricsQuery += "last_up_time = '" + lastUpTime + "', ";
            }
            updateMetricsQuery += "last_check_time = '" + lastCheckTime + "' WHERE path = '" + path + "'";
            jdbcTemplate.update(updateMetricsQuery);
        }
    }

    public boolean exist(String path) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM metrics WHERE path=?", new Object[]{path},
                Integer.class);
        return count > 0;
    }

    public List<Metrics> findAllMetrics() {
        String sql = "select bridge_id, name, type, path, status, last_up_time, last_check_time from metrics";
        return jdbcTemplate.query(sql, this::mapToMetrics);
    }

    private Metrics mapToMetrics(ResultSet resultSet, int rowNum) throws SQLException {
        String bridgeId = resultSet.getString("bridge_id");
        String name = resultSet.getString("name");
        String type = resultSet.getString("type");
        String path = resultSet.getString("path");
        String status = resultSet.getString("status");
        LocalDateTime lastUpTime = getLocalDateTime(resultSet, "last_up_time");
        LocalDateTime lastCheckTime = getLocalDateTime(resultSet, "last_check_time");
        return Metrics.builder()
                .bridgeId(bridgeId)
                .name(name)
                .type(type)
                .path(path)
                .status(Status.valueOf(status))
                .lastUpTime(lastUpTime)
                .lastCheckTime(lastCheckTime)
                .build();
    }

    private LocalDateTime getLocalDateTime(ResultSet resultSet, String columnLabel) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnLabel);
        return timestamp !=null ? timestamp.toLocalDateTime() : null;
    }
}

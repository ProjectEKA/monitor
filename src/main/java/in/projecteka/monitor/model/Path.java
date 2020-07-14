package in.projecteka.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Path {
    private List<String> bridgeUrls;
}

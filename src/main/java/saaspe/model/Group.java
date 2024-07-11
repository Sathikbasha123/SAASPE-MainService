package saaspe.model;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Group {

    List<String> keys;

    Map<String, MetricValue> metrics;
    
}

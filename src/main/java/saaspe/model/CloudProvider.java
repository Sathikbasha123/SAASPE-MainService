package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class CloudProvider {
    private String name;
    private List<CostInfo> cost;
}


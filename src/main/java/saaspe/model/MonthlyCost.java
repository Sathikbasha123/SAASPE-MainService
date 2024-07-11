package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class MonthlyCost {
    private List<CostInfo> cost;
}


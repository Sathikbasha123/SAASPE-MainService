package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CostInfo {
    private BigDecimal value;
    private String currency;
    public void setValueInINR(BigDecimal totalCostInINR) {
        this.value = totalCostInINR;
    }

   
    public void setValueInUSD(BigDecimal totalCostInUSD) {
        this.value = totalCostInUSD;
    }
}

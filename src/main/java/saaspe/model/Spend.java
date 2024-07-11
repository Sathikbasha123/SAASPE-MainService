package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Spend {

    private BigDecimal amount;

    private String unit;
    
}

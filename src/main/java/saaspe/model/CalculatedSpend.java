package saaspe.model;

import lombok.Data;

@Data
public class CalculatedSpend {

    private Spend actualSpend;

    private Spend forecastedSpend;
    
}

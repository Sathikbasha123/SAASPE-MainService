package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ForecastMonthAndCost {
	
	private String month;
	
	private BigDecimal cost;

}

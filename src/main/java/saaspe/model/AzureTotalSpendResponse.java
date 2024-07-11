package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class AzureTotalSpendResponse {

	private BigDecimal thisYearSpend;

	private BigDecimal thisMonthSpend;
	
	private String currency;

}

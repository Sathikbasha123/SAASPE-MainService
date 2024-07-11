package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OptimizeRequestAws {

	private String accountId;
	private String emailAddress;
	private String instanceId;
	private String resourceType;
	private String recommendationType;
	private String cpu;
	private BigDecimal monthlySavings;
	private BigDecimal annualSavings;
}

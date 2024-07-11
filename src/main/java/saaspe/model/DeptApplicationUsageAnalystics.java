package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DeptApplicationUsageAnalystics {
	private String applicationId;
	private String applicationName;
	private int userCount;
	private BigDecimal totalApplicationCost;
	private BigDecimal totalApplicationAdminCost;
	private String Currency;

	public DeptApplicationUsageAnalystics() {
		this.totalApplicationCost = BigDecimal.valueOf(0);
		this.totalApplicationAdminCost = BigDecimal.valueOf(0);
	}
}

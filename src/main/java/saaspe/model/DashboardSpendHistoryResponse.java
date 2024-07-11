package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class DashboardSpendHistoryResponse {
	private String month;
	private BigDecimal applicationSpend;
	private BigDecimal adminCost;
	private int applicationCount;
	private String currency = "USD";
	@JsonIgnore
	private int refId;
	// @JsonIgnore
	private List<String> appCount;

	public DashboardSpendHistoryResponse() {
		this.applicationSpend = BigDecimal.valueOf(0.0);
		this.adminCost = BigDecimal.valueOf(0.0);
		this.applicationCount = 0;
	}

}

package saaspe.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class getTopAppByMetricsResponse {

	private String logoUrl;

	private String applicationId;

	private String applicationName;

	private BigDecimal cost;

	private BigDecimal adminCost;

	private Integer userCount;

	private String currencyCode;

	private String projectName;

	private String projectCode;

	private String projectId;
}

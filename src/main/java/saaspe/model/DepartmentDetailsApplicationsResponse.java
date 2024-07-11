package saaspe.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailsApplicationsResponse {

	private String applicationLogo;
	private String applicationName;
	private String applicationStatus;
	private Integer applicationUserCount;
	private BigDecimal applicationSpend;
	private BigDecimal adminApplicationSpend;
	private String currencySymbol;

}

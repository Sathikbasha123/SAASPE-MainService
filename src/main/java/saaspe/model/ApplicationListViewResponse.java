package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApplicationListViewResponse {

	private String applicationLogo;

	private String applicationName;

	private String applicationCategory;

	private Integer applicationActiveUsers;

	private Integer applicationLicenses;

	private BigDecimal applicationSpend;

	private BigDecimal totalSpend;

	private Integer applicationContracts;

	private String currencyCode;

	private String applicationId;

	private String departmentId;

	private String departmentName;

	private List<newApplicationOwnerDetailsRequest> owners;

	private BigDecimal adminCost;

	private BigDecimal adminCostYtd;

}

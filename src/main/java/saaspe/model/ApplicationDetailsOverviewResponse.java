package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailsOverviewResponse {

	private String applicationLogo;

	private String applicationName;

	private String applicationDescription;

	private String applicationProviderName;

	private String applicationLink;

	private String applicationId;

	private BigDecimal applicationTotalSpend;

	private BigDecimal applicationAvgMonthlySpend;

	private Integer applicationAvgUsage;

	private Integer applicationActiveUserCount;

	private Integer applicationActiveContracts;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date applicationUpcomingRenewal;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date applicationReminderDate;

	private String applicationCategory;

	private String applicationDepartment;

	private String departmentId;

	private List<newApplicationOwnerDetailsRequest> ownerDetails;

	private String applciationStatus;

	private Boolean autoRenew;

	private String currencyCode;

	private String paymentMethod;

	private String cardholderName;

	private String cardNumber;

	private String validThrough;

	private String walletName;

	private Integer totalLicenses;

	private Integer mappedLicenses;

	private Integer unmappedLicenses;

	private String identityProvider;

	private Boolean isApplicationMapped;

	private Boolean isSsoIntegrated;

	private BigDecimal adminCost;

	private BigDecimal adminAvgCost;
	
	private Boolean isAdaptorConnected;
	
	private Boolean isAdaptorAvailable;

}

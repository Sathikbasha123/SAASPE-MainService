package saaspe.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsOverviewResponse {

	private String userName;
	private String userEmail;
	private String userLogo;
	private String userStatus;
	private String userDepartmentName;
	private String userDepartmentId;
	private String userDesignation;
	private String userType;
	private String userReportingManager;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date userOnboardedDate;
	private BigDecimal userAvgMonthlySpend;
	private BigDecimal userAvgMonthlyAdminSpend;
	private Integer userAvgUsage;
	private Integer userActiveApplications;
	private Integer userApplicationsCount;

}

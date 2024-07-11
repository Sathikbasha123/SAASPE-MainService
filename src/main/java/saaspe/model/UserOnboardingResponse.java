package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saaspe.entity.DepartmentDetails;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserOnboardingResponse {

	private String userId;

	private String firstName;

	private String lastName;

	private String userEmail;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date onboardDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date joiningDate;

	private String userDesignation;

	private DepartmentDetails departmentId;

	private String userReportingManager;

	private String userType;

	private String gender;

	private String requestNumber;

	private String childRequestNumber;

	private String mobileNumber;

	private String passWord;

	private String securityQuestion;

	private String securityAnsewer;

	private String opID;

	private String buID;

	private String createdBy;

	private String updatedBy;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String workGroup;

	private String approvedRejected;

	private String workGroupUserEmail;

	private String comments;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	private String logoUrl;

	private String onboardedByUserEmail;
}

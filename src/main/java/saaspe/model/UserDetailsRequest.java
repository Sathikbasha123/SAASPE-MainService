package saaspe.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saaspe.entity.ApplicationDetails;
import saaspe.entity.DepartmentDetails;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "saaspe_user_details")
public class UserDetailsRequest {

	private String userEmail;

	private String userName;

	private String userType;

	private String userDesigination;

	private String userReportingManager;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date joiningDate;

	private String team;

	private String userStatus;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String createdBy;

	private String updatedBy;

	private String buID;

	private String opID = "SAASPE";

	private String jobLevel;

	private String externalId4;

	private String externalId5;

	private String logoUrl;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date lastLoginTime;

	private List<ApplicationDetails> applicationId = new ArrayList<>();

	private String userId;

	private DepartmentDetails departmentId;

	private String userRole;

	private String userAccess;

}

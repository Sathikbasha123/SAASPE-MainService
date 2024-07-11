package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CreateUserDetails {
	private String userFirstName;
	private String userLastName;
	private String userEmailAddress;
	private String userDepartment;
	private String userDepartmentName;
	private String userReportingManager;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date userJoiningDate;
	private String userType;
	private String userDesignation;
	private String userGender;
	private String userMobileNumber;
	private String logoUrl;
}

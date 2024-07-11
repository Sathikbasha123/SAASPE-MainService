package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserInfo {

	private String userFirstName;
	private String userLastName;
	private String userEmailAddress;
	private String userDepartment;
	private String userReportingManager;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date userJoiningDate;
	private String userType;
	private String userDesignation;
	private String userGender;
	private String userMobileNumber;
	private boolean isSingle;

}

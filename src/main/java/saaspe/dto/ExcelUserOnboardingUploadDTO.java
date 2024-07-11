package saaspe.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelUserOnboardingUploadDTO {

	private String firstName;

	private String lastName;

	private String emailAddress;

	private String department;

	private String reportingManager;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-DD")
	private Date joiningDate;

	private String typeOfEmployment;

	private String designation;

	private String gender;

	private String contactNumber;

}

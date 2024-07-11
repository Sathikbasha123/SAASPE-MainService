package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "SAASPE_USER_LOGIN_DETAILS")
public class UserLoginDetails {

	@Column(name = "FIRST_NAME")
	private String firstName;

	@Column(name = "LAST_NAME")
	private String lastName;

	@Id
	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "DESIGNATION")
	private String designation;

	@Column(name = "SECURITY_QUESTION")
	private String securityQuestion;

	@Column(name = "SECURITY_ANSWER")
	private String securityAnswer;

	@Column(name = "OPID")
	private String opID;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;
}

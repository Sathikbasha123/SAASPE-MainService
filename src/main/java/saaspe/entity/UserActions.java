package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "saaspe_user_actions")
public class UserActions {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TRACEID")
	private String traceId;

	@Column(name = "USEREMAIL")
	private String userEmail;

	@Column(name = "ROLE")
	private String role;

	@Column(name = "ACTIONS")
	private String actions;

	@Column(name = "ENDPOINT")
	private String endpoint;

	@Column(name = "REQUEST")
	private String request;

	@Column(name = "RESPONSE")
	private String response;

	@Column(name = "METHOD_TYPE")
	private String methodType;

	@Column(name = "STATUS_CODE")
	private int statusCode;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "CREATED_ON")
	private Date createdOn;

	@Column(name = "SESSION_GROUP_ID")
	private String sessionGroupId;

	@Column(name = "TIME_TAKEN")
	private String timeTaken;

	@Column(name = "ACTION")
	private String action;

	@Column(name = "ACTION_CATEGORY")
	private String actionCategory;

	@Column(name = "ACTION_SUMMARY")
	private String actionSummary;

	@Column(name = "STATUS")
	private String status;

}

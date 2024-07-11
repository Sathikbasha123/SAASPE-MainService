package saaspe.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class UserActionListView {
	private String userEmail;
	private String actionSummary;
	private String actionCategory;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createdOn;
	private String role;
	private int statusCode;
	private String action;
	private String traceId;
	private String request;
	private String status;

}

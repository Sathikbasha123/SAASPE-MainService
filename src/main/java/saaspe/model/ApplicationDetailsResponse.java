package saaspe.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import saaspe.entity.ApplicationDetails;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApplicationDetailsResponse {

	private String categoryId;

	private String applicationName;

	private String applicationId;

	private String providerId;

	private String owner;

	private String ownerDepartment;

	private String tags;

	private String applicationStatus;

	private Integer activeContracts;

	private String applicationDescription;

	private String applicationPageUrl;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date renewalDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String createdBy;

	private String updatedBy;

	private String buID;

	private String opID;

	private String logoUrl;

	private String message;

	private Integer size;

	private List<ApplicationDetails> data;

	private ApplicationDetails details;

}

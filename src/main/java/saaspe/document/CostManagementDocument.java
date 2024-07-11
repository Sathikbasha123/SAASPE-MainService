package saaspe.document;

import java.util.Date;
import java.util.Properties;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "costManagement")
public class CostManagementDocument {

	@Transient
	public static final String SEQUENCE_NAME = "costAndUsagesequence";

	@Id
	private long id;

	private String name;

	private String type;

	private Properties properties;

	private String userId;

	private String subscriptionId;

	private String category;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private long amigoId;

}

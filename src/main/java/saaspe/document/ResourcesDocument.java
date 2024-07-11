package saaspe.document;

import java.util.Date;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AzureResources")
public class ResourcesDocument {

	@Transient
	public static final String SEQUENCE_NAME = "resources_sequence";

	@Id
	private long id;

	private String name;

	private String type;

	private String location;

	private String resourceId;

	private Map<String, String> department;

	private String managedBy;

	private Map<String, String> tags;

	private String subscriptionId;

	private String uuid;

	private String clientId;

	private String resourceGroupId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private String opID;

	private String buID;
	
	private long amigoId;

}

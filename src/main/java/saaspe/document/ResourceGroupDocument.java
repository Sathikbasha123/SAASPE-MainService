package saaspe.document;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AzureResourceGroup")
public class ResourceGroupDocument {

	@Transient
	public static final String SEQUENCE_NAME = "resource_group_sequence";

	@Id
	private long id;

	private String name;

	private String type;

	private String location;

	private String resourceGroupId;

	private String provisioningState;

	private String subscriptionId;

	private String owner_uuid;

	private long owner_id;

	private String purpose;

	private String environment;

	private boolean is_default;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date created_at;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updated_at;

	private String clientId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}

package saaspe.document;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.ComplianceDetails;
import saaspe.model.Tag;

@Data
@Document(collection = "AwsResourcesDocument")
public class AwsResourcesDocument {

	@Transient
	public static final String SEQUENCE_NAME = "awsResourcesDocumentsequence";

	private Long id;

	private String accountId;

	private String region;

	// ResourceTagMapping

	private String resourceARN;

	private List<Tag> tags;

	private ComplianceDetails complianceDetails;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	
	private String opID;

	private String buID;
	
	private long amigoId;

}

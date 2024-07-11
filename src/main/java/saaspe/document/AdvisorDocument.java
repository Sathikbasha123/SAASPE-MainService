package saaspe.document;

import java.util.Date;
import java.util.Map;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import saaspe.model.ShortDescription;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AdvisorDocument")
public class AdvisorDocument {

	@Transient
	public static final String SEQUENCE_NAME = "advisorDocumentsequence";

	private String id;
	private String type;
	private String name;

	private String subscriptionId;
	// properties

	private String category;
	private String impact;
	private String impactedField;
	private String impactedValue;
	private String lastUpdated;
	private String recommendationTypeID;
	private ShortDescription shortDescription;
	private Map<String, String> extendedProperties;
	private String resourceId;
	private String[] suppressionIDS;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	private String opID;
	private String buID;
	private long amigoId;
	
	
}

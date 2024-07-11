package saaspe.docusign.document;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document(collection = "ClmTemplateDocument")
public class CreateTemplate {

	@Transient
	public static final String SEQUENCE_NAME = "clmTemplateDocumentsequecnce";

	@Id
	@JsonIgnore
	private long id;

	private String templateId;

	private String templateName;
	
	private String opID;

	private String buID;
	
	private long amigoId;
}

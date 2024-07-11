package saaspe.docusign.document;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.docusign.model.EventFieldsList;

@Data
@Document(collection = "AuditEventDocument")
public class AuditEventDocument {

	@Transient
	public static final String SEQUENCE_NAME = "auditEventDocumentsequence";

	private long id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;
	
	private String envelopeId;
	
	private List<EventFieldsList> auditEvents;
	
	private String opID;

	private String buID;
	
	private long amigoId;
	
}

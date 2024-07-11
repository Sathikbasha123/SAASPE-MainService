package saaspe.docusign.document;

import java.util.Date;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.docusign.model.EventData;

@Data
@Document(collection = "EventDocument")
public class EventDocument {

	@Transient
	public static final String SEQUENCE_NAME = "eventDocumentsequence";

	private long id;
	
	private String event;
	
	private String apiVersion;
	
	private String uri;
	
	private String retryCount;
	
	private String configurationId;
	
	private String generatedDateTime;
	
	private EventData data;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;
	
	private String opID;

	private String buID;
	
	private long amigoId;

}

package saaspe.docusign.document;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.docusign.model.DocumentResponse;

@Data
@Document(collection = "EnvelopeDocument")
public class EnvelopeDocument {

	@Transient
	public static final String SEQUENCE_NAME = "envelopeDocumentsequence";

	private long id;

	private Object envelope;

	private String envelopeId;

	private List<DocumentResponse> documents;

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

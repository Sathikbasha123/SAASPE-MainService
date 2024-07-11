package saaspe.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.AskamigoResponse;

@Data
@Document(collection = "ConversationDocument")
public class ConversationDocument {

	@Transient
	public static final String SEQUENCE_NAME = "conversationDocumentsequence";

	@Id
	private Long id;

	private String conversationId;

	private String userId;

	private String userEmail;

	private String query;

	private AskamigoResponse response;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	private Boolean like;

	private String comments;

	private String opID;

	private String buID;
	
	private long amigoId;
}

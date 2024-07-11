package saaspe.document;

import java.util.Date;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.CurrentInstance;

@Data
@Document(collection = "AwsRightsizingRecommendationDocument")
public class AwsRightsizingRecommendationDocument {

	@Transient
	public static final String SEQUENCE_NAME = "awsRightsizingRecommendationDocumentDocumentsequence";

	private Long id;

	private String accountId;

	private CurrentInstance currentInstance;

	private String rightsizingType;

	private Object modifyRecommendationDetail;

	private Object terminateRecommendationDetail;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}

package saaspe.document;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.Group;
import saaspe.model.MetricValue;
import saaspe.model.TimePeriod;

@Data
@Document(collection = "AwsCostAndUsageMonthly")
public class AwsCostAndUsageMonthlyDocument {

	@Transient
	public static final String SEQUENCE_NAME = "costAndUsageMonthlyDocumentsequence";

	@Id
	private Long id;

	private String accountId;

	private TimePeriod timePeriod;

	private Map<String, MetricValue> total;

	private List<Group> groups;

	private Boolean estimated;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}

package saaspe.document;

import java.util.Date;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.MetricValue;
import saaspe.model.TimePeriod;

@Data
@Document(collection = "AwsCostForecast")
public class AwsCostForecastDocument {

	@Transient
	public static final String SEQUENCE_NAME = "awsCostForecastDocumentsequence";

	private Long id;

	private MetricValue totalCost;

	private TimePeriod timePeriod;

	private String meanValue;

	private String predictionIntervalLowerBound;

	private String predictionIntervalUpperBound;

	private String accountId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}

package saaspe.document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.QueryColumnsResponse;

@Data
@Document(collection = "AzureForecast")
public class AzureForecastDocument {

	@Transient
	public static final String SEQUENCE_NAME = "azureForecastDocumentsequence";

	@Id
	private Long id;

	private String name;

	private String type;

	private List<QueryColumnsResponse> column;

	private List<List<Object>> rows;

	private String subscriptionId;

	private String resourceName;

	private BigDecimal cost;

	private Date usageDate;

	private String currency;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;
	private String opID;

	private String buID;
	
	private long amigoId;

}

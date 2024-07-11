package saaspe.document;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import saaspe.model.QueryColumnsResponse;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "CostAndUsage")
public class CostManagementUsageDocument {

	@Transient
	public static final String SEQUENCE_NAME = "costAndUsagesequence";

	@Id
	private long id;

	private String AzureId;

	private String name;

	private String type;

	private List<QueryColumnsResponse> column;

	private List<List<Object>> rows;

	private String SubsciptionId;

	private String location;

	private String sku;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private long amigoId;

}

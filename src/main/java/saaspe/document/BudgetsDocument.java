package saaspe.document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AzureBudgets")
public class BudgetsDocument {

	private long Id;

	private String clientId;

	private String subscriptionId;

	private String subscriptionName;

	private String budgetId;

	private String name;

	private String type;

	private String eTag;

	private String startDate;

	private String endDate;

	private String timeGrain;

	private BigDecimal amount;

	private BigDecimal currentSpendAmount;

	private String unit;

	private String category;

	private BigDecimal threshold;

	private List<String> contactEmails;

	private boolean emailTrigger;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}

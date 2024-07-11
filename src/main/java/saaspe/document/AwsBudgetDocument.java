package saaspe.document;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import saaspe.model.Action;
import saaspe.model.BudgetTimePeriod;
import saaspe.model.CalculatedSpend;
import saaspe.model.Notification;
import saaspe.model.Spend;
import saaspe.model.Subscriber;

@Data
@Document(collection = "AwsBudget")
public class AwsBudgetDocument {

	@Transient
	public static final String SEQUENCE_NAME = "awsBudgetDocumentsequence";

	private Long id;

	private String budgetName;

	private Spend budgetLimit;

	private Object costTypes;

	private Map<String, Object> plannedBudgetLimits;

	private Map<String, List<String>> costFilters;

	private String timeUnit;

	private BudgetTimePeriod timePeriod;

	private CalculatedSpend calculatedSpend;

	private String budgetType;

	private Date lastUpdatedTime;

	private String accountId;

	private List<Action> actions;

	private List<Notification> notifications;

	private List<Subscriber> subscriber;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	private String opID;

	private String buID;
	
	private long amigoId;
}
package saaspe.document;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Id;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;


@Data
@JsonInclude(Include.NON_NULL)
@Document(collection = "AzureSpendingHistory")
public class AzureSpendingHistoryDocument {
	@Transient
	public static final String SEQUENCE_NAME = "azureSpendingHistoryDocumentsequence";

	@Id
	private Long id;
	private String cloudProvider;
    private String month;
    private int year;
    private BigDecimal totalCostINR;
    private BigDecimal totalCostUSD;

   
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;
}
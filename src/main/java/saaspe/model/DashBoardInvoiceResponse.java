package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class DashBoardInvoiceResponse {

    private Integer totalInvoices;

    private Integer paidInvoices;

    private Integer pendingInvoices;

    private BigDecimal totalInvoicesCost;

    private BigDecimal paidInvoicesCost;

    private BigDecimal pendingInvoicesCost;
}

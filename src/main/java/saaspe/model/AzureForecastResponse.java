package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class AzureForecastResponse extends Response {

	private String name;

	private BigDecimal actualCost;

	private String currency;

	private String id;

	private AzureForecastData forecastData;

}

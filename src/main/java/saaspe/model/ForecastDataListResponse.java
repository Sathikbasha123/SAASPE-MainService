package saaspe.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForecastDataListResponse extends Response {

	private String name;

	private BigDecimal actualCost;

	private String currency;

	private String id;

	private List<ForecastMonthAndCost> forecastData;
}

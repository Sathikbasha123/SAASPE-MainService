package saaspe.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Rates {

	@JsonProperty(value = "MYR")
	private BigDecimal MYR;

	@JsonProperty(value = "INR")
	private BigDecimal INR;

	@JsonProperty(value = "USD")
	private BigDecimal USD;

	@JsonProperty(value = "AUD")
	private BigDecimal AUD;

	@JsonProperty(value = "PHP")
	private BigDecimal PHP;

	@JsonProperty(value = "SGD")
	private BigDecimal SGD;

	@JsonProperty(value = "CAD")
	private BigDecimal CAD;

	@JsonProperty(value = "AED")
	private BigDecimal AED;

	@JsonProperty(value = "GBP")
	private BigDecimal GBP;

	@JsonProperty(value = "EUR")
	private BigDecimal EUR;
}

package saaspe.model;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CurrencyConverterResponse {
	private boolean success;
	@JsonIgnore
	private Timestamp timestamp;
	private boolean historical;
	private String base;
	private String date;
	private Rates rates;
	private String message;
}

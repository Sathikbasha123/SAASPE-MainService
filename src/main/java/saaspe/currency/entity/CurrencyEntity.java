package saaspe.currency.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CURRENCY_ENTITY")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_entity_seq")
	@SequenceGenerator(name = "currency_entity_seq", sequenceName = "currency_entity_seq")
	private Integer id;

	@Column(name = "date")
	private Date date;

	@Column(name = "AUD")
	private BigDecimal aud;

	@Column(name = "USD")
	private BigDecimal usd;

	@Column(name = "MYR")
	private BigDecimal myr;

	@Column(name = "INR")
	private BigDecimal inr;

	@Column(name = "PHP")
	private BigDecimal php;

	@Column(name = "SGD")
	private BigDecimal sgd;

	@Column(name = "CAD")
	private BigDecimal cad;

	@Column(name = "AED")
	private BigDecimal aed;

	@JsonProperty(value = "EUR")
	private BigDecimal eur;

	@JsonProperty(value = "gbp")
	private BigDecimal gbp;

	@Column(name = "BASE")
	private String base;
}

package saaspe.currency.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "CURRENCY_API_KEY")
public class ApiKeys {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_entity_seq")
	@SequenceGenerator(name = "currency_entity_seq", sequenceName = "currency_entity_seq")
	private Long id;

	@Column(length = 100000, name = "API_KEY")
	private String apiKey;

	@Column(name = "TEMPLATE")
	private String template;
}

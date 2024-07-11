package saaspe.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "saaspe_adaptor_credential")
public class AdaptorCredential {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;

	@Column(name = "APPLICATION_NAME")
	private String applicationName;

	@Column(name = "ADAPTOR_KEYS")
	private String adaptorKeys;

	@Column(name = "FLOW_TYPE")
	private String flowType;

	@Column(name = "ACCESSTOKEN_ENDPOINT")
	private String accessTokenEndpoint;

}

package saaspe.document;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "AzureSubscriptions")
public class AzureSubscriptions {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;

	@Column(name = "subscriptionId")
	private String subscriptionId;

	@Column(name = "displayName")
	private String displayName;

	@Column(name = "clientId")
	private String clientId;

	private String opID;

	private String buID;
	
	private long amigoId;
}

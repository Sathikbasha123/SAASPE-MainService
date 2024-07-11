package saaspe.docusign.model;

import lombok.Data;

@Data
public class EventData {

	private String accountId;
	private String userId;
	private String templateId;
	private String name;
	private String created;
	private String envelopeId;
	private String recipientId;
	private Object envelopeSummary;

}

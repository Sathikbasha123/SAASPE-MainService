package saaspe.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ClmContractRequest {

	private String contractName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractStartDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date contractEndDate;

	private int renewalReminderNotification;

	private List<Document> documents;

	private String emailSubject;

	private String emailMessage;

	private Boolean signingOrder;

	private Recipients recipients;

	private String status;

	private String allowComments;

	private Boolean enforceSignerVisibility;

	private String recipientLock;

	private String messageLock;

	private Reminders reminders;

	private Expiration expirations;

	private Boolean signerCanSignOnMobile;

	private Boolean reviewerSigningOrder;
	private Boolean signerSigningOrder;

	@Data
	public class Reminders {
		private String reminderDelay;
		private String reminderFrequency;
		private String reminderEnabled;
	}

	@Data
	public class Expiration {
		private String expireAfter;
		private String expireEnabled;
		private String expireWarn;
	}

	private int contractPeriod;

	private String userEmail;

}

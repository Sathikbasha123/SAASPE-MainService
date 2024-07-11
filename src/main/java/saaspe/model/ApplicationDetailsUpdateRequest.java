package saaspe.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailsUpdateRequest {

	private String applicationCetegory;
	private String applicationOwnerDepartment;
	private String applicationOwnerEmail;
	private boolean autoRenewal;
	private Date paymentMethod;
	private String cardholderName;
	private String cardNumber;
	private String validThrough;
	private String walletName;
	private String applicationLink;
}

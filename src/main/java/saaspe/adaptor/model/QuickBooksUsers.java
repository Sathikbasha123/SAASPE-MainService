package saaspe.adaptor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuickBooksUsers {
	
	private String userName;
	private String userId;
	private String userEmail;

}
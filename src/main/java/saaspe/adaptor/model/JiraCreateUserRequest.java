package saaspe.adaptor.model;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JiraCreateUserRequest {
	private String emailAddress;
	private List<String> products;
}

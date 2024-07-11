package saaspe.model;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class SupportDocumentsResponse {
	
	private String fileName;

	private List<URI> fileUrl;
}

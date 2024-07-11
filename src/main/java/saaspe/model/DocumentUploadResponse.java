package saaspe.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUploadResponse extends Response {

	private String filePath;
}

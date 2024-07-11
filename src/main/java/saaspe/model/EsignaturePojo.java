package saaspe.model;

import java.util.List;

import lombok.Data;

@Data
public class EsignaturePojo {

	private List<Document> documents;
	private String emailSubject;
	private Boolean signingOrder;
	private Recipients recipients;
	private String status;

}

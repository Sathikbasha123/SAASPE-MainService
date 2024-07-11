package saaspe.docusign.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EnvelopeDocument {
	
	@JsonProperty("addedRecipientIds")
	  private java.util.List<String> addedRecipientIds = null;

	  @JsonProperty("attachmentTabId")
	  private String attachmentTabId = null;

	  @JsonProperty("authoritativeCopy")
	  private String authoritativeCopy = null;

	  @JsonProperty("authoritativeCopyMetadata")
	  private PropertyMetadata authoritativeCopyMetadata = null;

	  @JsonProperty("availableDocumentTypes")
	  private java.util.List<SignatureType> availableDocumentTypes = null;

	  @JsonProperty("containsPdfFormFields")
	  private String containsPdfFormFields = null;

	  @JsonProperty("display")
	  private String display = null;

	  @JsonProperty("displayMetadata")
	  private PropertyMetadata displayMetadata = null;

	  @JsonProperty("documentBase64")
	  private String documentBase64 = null;

	  @JsonProperty("documentFields")
	  private java.util.List<NameValue> documentFields = null;

	  @JsonProperty("documentId")
	  private String documentId = null;

	  @JsonProperty("documentIdGuid")
	  private String documentIdGuid = null;

	  @JsonProperty("errorDetails")
	  private ErrorDetails errorDetails = null;

	  @JsonProperty("includeInDownload")
	  private String includeInDownload = null;

	  @JsonProperty("includeInDownloadMetadata")
	  private PropertyMetadata includeInDownloadMetadata = null;

	  @JsonProperty("name")
	  private String name = null;

	  @JsonProperty("nameMetadata")
	  private PropertyMetadata nameMetadata = null;

	  @JsonProperty("order")
	  private String order = null;

	  @JsonProperty("pages")
	  private java.util.List<Page> pages = null;

	  @JsonProperty("signerMustAcknowledge")
	  private String signerMustAcknowledge = null;

	  @JsonProperty("signerMustAcknowledgeMetadata")
	  private PropertyMetadata signerMustAcknowledgeMetadata = null;

	  @JsonProperty("sizeBytes")
	  private String sizeBytes = null;

	  @JsonProperty("templateLocked")
	  private String templateLocked = null;

	  @JsonProperty("templateRequired")
	  private String templateRequired = null;

	  @JsonProperty("type")
	  private String type = null;

	  @JsonProperty("uri")
	  private String uri = null;

}

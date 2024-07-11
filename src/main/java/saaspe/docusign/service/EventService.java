package saaspe.docusign.service;

import java.text.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.model.CommonResponse;

public interface EventService {

	String handleEvent(String body) throws JsonProcessingException, ParseException;

	CommonResponse getAuditEventsFromDocusign() throws JsonProcessingException;

	CommonResponse getAuditEvents(String envelopeId);

}

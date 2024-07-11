package saaspe.docusign.controller;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import saaspe.aspect.ControllerLogging;
import saaspe.docusign.service.EventService;
import saaspe.model.CommonResponse;

@RestController
@RequestMapping("/docusign")
@ControllerLogging
public class EventController {

	@Autowired
	private EventService eventService;

	private static final Logger log = LoggerFactory.getLogger(EventController.class);

	@PostMapping("/events")
	public String handleEvent(@RequestBody String body) throws JsonProcessingException, ParseException {
		log.info("handleEvent start");
		eventService.handleEvent(body);
		log.info("handleEvent end");
		return null;
	}

	@GetMapping("/s")
	@Scheduled(cron = "0 */15 * * * *")
	public CommonResponse getAuditEventsFromDocusign() throws JsonProcessingException {
		log.info("getAuditEventsFromDocusign log");
		return eventService.getAuditEventsFromDocusign();
	}

	@GetMapping("/audit/{envelopeId}")
	public CommonResponse getAuditEvents(@PathVariable String envelopeId) {
		log.info("getAuditEvents log");
		return eventService.getAuditEvents(envelopeId);
	}

	@PostMapping("/evented")
	public ResponseEntity<CommonResponse> handleEvents() {
		CommonResponse response = new CommonResponse();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}

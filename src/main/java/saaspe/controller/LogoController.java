package saaspe.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.LogoService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application")
public class LogoController {

	@Autowired
	private LogoService logoService;

	private static final Logger log = LoggerFactory.getLogger(LogoController.class);

	@GetMapping("/get-logos")
	public ResponseEntity<CommonResponse> getApplicatoinLogoDetails(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			log.info("*** Enter into addApplicationLogoDetails method ***");
			CommonResponse response = logoService.getApplicatoinLogoDetails();
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.error("*** Ending addApplicationLogoDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("LogoResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}

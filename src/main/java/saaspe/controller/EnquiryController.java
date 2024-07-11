package saaspe.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.dto.EnquiryRequest;
import saaspe.exception.DataValidationException;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.SiteVerificationRequest;
import saaspe.service.EnquiryService;

@RestController
@RequestMapping("/api/")
@ControllerLogging
public class EnquiryController {

	@Autowired
	private EnquiryService enquiryService;

	private static final Logger log = LoggerFactory.getLogger(EnquiryController.class);

	@PostMapping("/enquiry")
	public ResponseEntity<CommonResponse> saveUserContactFormDetails(@RequestBody EnquiryRequest enquiryRequest,
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse resp = enquiryService.saveUserContactFormDetails(enquiryRequest);
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			log.error("**saveUserContractForm method ending with error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("saveUserContractForm", ""), e.getMessage()));
		}

	}

	@PostMapping("/siteverify")
	public ResponseEntity<CommonResponse> siteVerify(@RequestBody SiteVerificationRequest response) {
		try {
			CommonResponse res = enquiryService.getSiteVerification(response);
			JSONObject jsonObject = new JSONObject(res);
			boolean successValue = jsonObject.getJSONObject("response").getJSONObject("data").getBoolean("success");
			if (!successValue) {
				return ResponseEntity.status(res.getStatus()).body(new CommonResponse(HttpStatus.BAD_REQUEST,
						new Response("Cloudflare Site verify", res.getResponse()), "Site verification failed"));
			}
			return new ResponseEntity<>(res, res.getStatus());
		} catch (DataValidationException e) {
			return new ResponseEntity<>(
					new CommonResponse(e.getStatusCode(), new Response(e.getErrorCode(), null), e.getMessage()),
					e.getStatusCode());
		}

		catch (Exception e) {
			log.error("**cloudFlare siteVerify method ending with error**", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("Cloudflare Site verify", ""), e.getMessage()));
		}
	}
}

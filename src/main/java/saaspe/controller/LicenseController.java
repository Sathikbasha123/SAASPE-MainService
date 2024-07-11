package saaspe.controller;

import java.util.ArrayList;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.aspect.ControllerLogging;
import saaspe.constant.Constant;
import saaspe.dto.UserLicenseAssignDto;
import saaspe.exception.DataValidationException;
import saaspe.model.ApplicationsLicenseCountRequest;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.LicenseService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application/license")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;

	private static final Logger log = LoggerFactory.getLogger(LicenseController.class);

	@GetMapping("/mapped/users")
	@PreAuthorize("hasAuthority('VIEW_USER')")
	public ResponseEntity<CommonResponse> getUsersDetailsByLicebseId(
			@Valid @RequestParam("licenseId") String licenseId) {
		try {
			log.info("*** Enter into getUsersDetailsByLicebseId method with request : {} ***", licenseId);
			CommonResponse applicationLicenseDetailsResponse = licenseService.getUsersDetailsByLicebseId(licenseId);
			return new ResponseEntity<>(applicationLicenseDetailsResponse, HttpStatus.OK);
		} catch (DataValidationException e) {
			log.error("*** Ending getUsersDetailsByLicebseId method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("licenseUsersResponse", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending getUsersDetailsByLicebseId method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LICENSE_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PutMapping("/link")
	@PreAuthorize("hasAuthority('EDIT_APPLICATION')")
	public ResponseEntity<CommonResponse> linkUserandLicense(@RequestBody UserLicenseAssignDto licenseAssignDto) {
		try {
			CommonResponse applicationLicenseDetailsResponse = licenseService.linkUserLicense(licenseAssignDto);
			return new ResponseEntity<>(applicationLicenseDetailsResponse,
					applicationLicenseDetailsResponse.getStatus());
		} catch (DataValidationException e) {
			log.error("*** Ending linkUserandLicense method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(e.getStatusCode(),
					new Response("assignLicenseRequest", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending linkUserandLicense method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LICENSE_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

	@PostMapping("/unmapped/count")
	@PreAuthorize("hasAuthority('EDIT_APPLICATION')")
	public ResponseEntity<CommonResponse> getApplicationsLicenseCount(
			@RequestBody ApplicationsLicenseCountRequest applicationLicenseDetails) {
		try {
			CommonResponse resp = licenseService.getAppplicationsLicenseCount(applicationLicenseDetails);
			return new ResponseEntity<>(resp, HttpStatus.CREATED);
		} catch (DataValidationException e) {
			log.error("*** Ending get application license count method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.NOT_FOUND,
					new Response("Application License Count Response", new ArrayList<>()), e.getMessage()));
		} catch (Exception e) {
			log.error("*** Ending get application license count method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response(Constant.LICENSE_RESPONSE, new ArrayList<>()), e.getMessage()));
		}
	}

}

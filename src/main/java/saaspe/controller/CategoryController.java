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

import io.swagger.annotations.ApiOperation;
import saaspe.aspect.ControllerLogging;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.service.CategoryService;

@RestController
@ControllerLogging
@RequestMapping("/api/v1/application")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

	@ApiOperation(value = "List Of Application Categories", notes = "Get the List Of Application Categories Avaliable")
	@GetMapping("/categories")
	public ResponseEntity<CommonResponse> getApplicationDetails(
			@RequestHeader(name = "opID", required = false, defaultValue = "SAASPE") String opID,
			@RequestHeader(name = "buID", required = false, defaultValue = "") String buID) {
		try {
			CommonResponse applicationCategoryResponse = categoryService.getApplicationCategoryDetails();
			return new ResponseEntity<>(applicationCategoryResponse, HttpStatus.OK);
		} catch (Exception e) {
			log.error("*** Ending getApplicationDetails method with an error ***", e);
			return ResponseEntity.badRequest().body(new CommonResponse(HttpStatus.BAD_REQUEST,
					new Response("ApplicationDetailsResponse", new ArrayList<>()), e.getMessage()));
		}
	}

}

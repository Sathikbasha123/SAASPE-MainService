package saaspe.sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saaspe.model.CommonResponse;
import saaspe.model.Response;

@RestController
@RequestMapping("/api/v1/sso")
public class SingleSignOnController {

	@Autowired
	SingleSignOnService signOnService;

	@PostMapping("/token")
	public ResponseEntity<CommonResponse> getAzureUserAccessAndRoles(@RequestParam("token") String token) {
		CommonResponse commonResponse = new CommonResponse();
		Response response = new Response();
		try {
			CommonResponse testdata = signOnService.getAzureUserAccessAndRoles(token);
			return ResponseEntity.ok().body(testdata);
		} catch (Exception e) {
			commonResponse.setMessage("User Not Authorized OR Dont Have privileges");
			commonResponse.setStatus(HttpStatus.BAD_REQUEST);
			response.setAction("User Access And Roles");
			response.setData("");
			commonResponse.setResponse(response);
			return ResponseEntity.badRequest().body(commonResponse);
		}
	}

}

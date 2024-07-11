package saaspe.aspect;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogMessage {
	private String className;
	private String methodName;
	private String requestUrl;
	private String requestFrom;
	private Map<Object, String> requestHeaders;
	private Map<Object, String> requestParameters;
	private Object[] methodArguments;
	private String methodResult;
	private long timeElapsed;
	private String traceId;
	private String method;
	private String requestUri;
	private String requestPayload;
	private int responseCode;
	private Object response;
	private long timeTaken;
	private String query;

}

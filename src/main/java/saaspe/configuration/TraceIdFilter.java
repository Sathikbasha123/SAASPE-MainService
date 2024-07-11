package saaspe.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

@Component
public class TraceIdFilter implements Filter {

	@Autowired
	private Tracer tracer;

	private static final String TRACE_ID_HEADER = "X-Trace-Id";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String traceId = generateTraceId(); 

		httpRequest.setAttribute(TRACE_ID_HEADER, traceId);
		httpResponse.setHeader(TRACE_ID_HEADER, traceId);


		chain.doFilter(request, response);
	}

	public TraceIdFilter(Tracer tracer) {
		super();
		this.tracer = tracer;
	}

	private String generateTraceId() {
		Span span = tracer.currentSpan();
		return span.context().traceId();
	}
}

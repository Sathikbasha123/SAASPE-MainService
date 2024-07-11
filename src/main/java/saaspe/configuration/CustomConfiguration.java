package saaspe.configuration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;

@Configuration
public class CustomConfiguration {
	@Autowired
	private AuthenticationManager authenticationManager;

	@Bean
	public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
		return request -> authenticationManager;
	}
}

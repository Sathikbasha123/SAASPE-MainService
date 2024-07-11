package saaspe.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.context.NoSuchMessageException;

import freemarker.template.TemplateException;
import saaspe.entity.UserLoginDetails;
import saaspe.exception.AuthenticationException;
import saaspe.exception.DataValidationException;
import saaspe.model.ChangePasswordRequest;
import saaspe.model.CommonResponse;
import saaspe.model.LoginRequest;
import saaspe.model.RefreshTokenRequest;
import saaspe.model.ResetPasswordRequest;
import saaspe.model.SignupRequest;
import saaspe.model.VerificationRequest;

public interface UserLoginService {

	CommonResponse createUserProfile(SignupRequest signupRequest) throws DataValidationException;

	CommonResponse login(LoginRequest request)
			throws AuthenticationException, DataValidationException, UnsupportedEncodingException, MessagingException;

	Optional<UserLoginDetails> loadUserByUsername(String userName);

	CommonResponse verifyEmail(String userId, String emailAddress, String verificationToken)
			throws AuthenticationException, UnsupportedEncodingException;

	CommonResponse changePassword(ChangePasswordRequest changePassword, UserLoginDetails profile)
			throws NoSuchMessageException, DataValidationException;

	CommonResponse verifyEmailInitiate(String emailAddress, String redirectUrl)
			throws AuthenticationException, IOException, TemplateException, MessagingException;

	CommonResponse resetInitiate(String emailAddress, String redirectUrl)
			throws DataValidationException, IOException, TemplateException, MessagingException;

	CommonResponse resetPassword(ResetPasswordRequest reset) throws DataValidationException;

	CommonResponse getUserAccessAndRole(UserLoginDetails profile);

	CommonResponse getUserAccessAndRole(HttpServletRequest request, UserLoginDetails profile, String xAuthProvider);

	CommonResponse getRefreshToken(RefreshTokenRequest refreshTokenRequest) throws DataValidationException;

	CommonResponse verifyOTP(@Valid VerificationRequest request) throws DataValidationException;

	CommonResponse sendConsentToUser(HttpServletRequest request, String xAuthProvider)
			throws MessagingException, DataValidationException, IOException, TemplateException;

	CommonResponse resetFailedCounts();

	String deleteUserinRedis(String email);

}
package saaspe.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.UserLastLoginDetails;

public interface UserLastLoginDetailRepository extends JpaRepository<UserLastLoginDetails, Long> {

	@Query("select a from UserLastLoginDetails a where a.userIdentityId =:identityId and a.applicationId =:applicationId")
	UserLastLoginDetails findByGraphUserIdAppId(String identityId, String applicationId);

	@Query(value = "SELECT * FROM user_last_login_details a where a.application_id =:applicationId AND a.last_login_time BETWEEN :startDate AND :endDate and a.end_date is null;", nativeQuery = true)
	List<UserLastLoginDetails> lastLoginUsersDataByDate(Date startDate, Date endDate, String applicationId);

	@Query(value = "SELECT * FROM user_last_login_details a where a.application_id =:applicationId AND a.last_login_time <:endDate and a.end_date is null;", nativeQuery = true)
	List<UserLastLoginDetails> lastLoginUsersDataafter90days(Date endDate, String applicationId);

	@Query(value = "SELECT * FROM user_last_login_details WHERE end_date IS NULL AND application_id =:applicationId AND last_login_time = (SELECT MIN(last_login_time) FROM user_last_login_details WHERE end_date IS NULL AND application_id =:applicationId);", nativeQuery = true)
	UserLastLoginDetails getLastUserLoginDate(String applicationId);

	@Query("select a from UserLastLoginDetails a where a.applicationId =:applicationId")
	List<UserLastLoginDetails> findApplicationId(String applicationId);

	@Query(value = "select * from user_last_login_details a where a.user_email=:userEmail and a.end_date is null ", nativeQuery = true)
	UserLastLoginDetails findByUsersEmail(String userEmail);
}

package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.entity.UserOnboarding;

@Repository
public interface UserOnboardingDetailsRepository extends JpaRepository<UserOnboarding, Integer> {

	UserOnboarding findByUserId(String userId);

	UserOnboarding findByChildRequestNumber(String childRequestNumber);

	UserOnboarding findByRequestNumber(String requestId);

	@Query("SELECT a FROM UserOnboarding a where a.requestNumber=:requestId and a.workGroup =:role and a.approvedRejected =:status")
	UserOnboarding findByRequestNumber(String requestId, String role, String status);

	@Query(value = "SELECT * FROM saaspe_user_onboarding a where a.work_group =:userRole and a.approved_rejected =:approve ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC,CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<UserOnboarding> getAllByName(String userRole, String approve);

	@Query("SELECT a FROM UserOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup =:role and a.approvedRejected =:status")
	UserOnboarding findByChildRequestNumber(String childRequestId, String role, String status);

	@Query("SELECT a FROM UserOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	UserOnboarding findAllBySuperAdmin(String childRequestId);

	@Query("SELECT a FROM UserOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	UserOnboarding findAllBySuperAdminRequestId(String requestId);

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM UserOnboarding c WHERE c.userEmail =:userEmailAddress ")
	boolean existByEmailAddress(String userEmailAddress);

	@Query(value = "SELECT * FROM saaspe_user_onboarding a where a.work_group IN ('Approver','Reviewer') and a.approved_rejected ='Review' ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC, CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<UserOnboarding> findAllByListView();

	@Query("SELECT a FROM UserOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	UserOnboarding findAllByChildReqReject(String childRequestId);

	@Query("SELECT a FROM UserOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('super_admin','superadmin','Approver') and a.approvedRejected ='Approve'")
	UserOnboarding findChildReqSuperApprovee(String childRequestId);

	@Query("SELECT a FROM UserOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review' and a.signUp=false")
	UserOnboarding requestTrackingStepOneReq(String requestId);

	@Query("SELECT a FROM UserOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected' and a.signUp=false")
	UserOnboarding requestTrackingStepTwoReq(String requestId);

	@Query("SELECT a FROM UserOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('super_admin','superadmin','Approver') and a.approvedRejected ='Approve' and a.signUp=false")
	UserOnboarding requestTrackingStepThreeReq(String requestId);

	@Query(value = "select * from saaspe_user_onboarding a where a.user_email =:userEmail order by a.created_on desc limit 1", nativeQuery = true)
	UserOnboarding findByEmailAddresss(String userEmail);

	@Query(value = "select * from saaspe_user_onboarding where user_email= :onboardingRequestName order by created_on desc limit 1 ;", nativeQuery = true)
	UserOnboarding getDataByApplicationName(String onboardingRequestName);

	@Query(value = "select * from saaspe_user_onboarding where sign_up = FALSE ;", nativeQuery = true)
	List<UserOnboarding> findAllBySignup();

	@Query(value = "select * from saaspe_user_onboarding where user_email = :userEmail and sign_up = FALSE order by created_on desc limit 1 ;", nativeQuery = true)
	UserOnboarding getByUserEmail(String userEmail);

	@Query(value = "select * from saaspe_user_onboarding a where a.mobile_number =:userMobileNumber and end_date is null order by a.created_on desc limit 1 ;", nativeQuery = true)
	UserOnboarding findByUserMobileNumber(String userMobileNumber);
	
	@Query(value="select * from  saaspe_user_onboarding where child_request_number =:childRequestId or request_number =:requestId order by created_on desc limit 1",nativeQuery=true)
	UserOnboarding findByRequest(String requestId,String childRequestId);

}
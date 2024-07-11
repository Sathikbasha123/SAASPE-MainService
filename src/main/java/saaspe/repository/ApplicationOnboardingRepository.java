package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationOnboarding;

public interface ApplicationOnboardingRepository extends JpaRepository<ApplicationOnboarding, Integer> {

	@Query("SELECT a FROM ApplicationOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	ApplicationOnboarding findAllBySuperAdmin(String childRequestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	ApplicationOnboarding findAllBySuperAdminRequestId(String requestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.requestNumber=:requestId and a.workGroup =:role and a.approvedRejected =:status")
	ApplicationOnboarding findByRequestNumber(String requestId, String role, String status);

	@Query("SELECT a FROM ApplicationOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup =:role and a.approvedRejected =:status")
	ApplicationOnboarding findByChildRequestNumber(String childRequestId, String role, String status);

	@Query(value = "SELECT * FROM saaspe_application_onboarding a WHERE a.work_group =:role AND a.approved_rejected =:key ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC,CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<ApplicationOnboarding> getAllByName(String role, String key);

	@Query(value = "SELECT * FROM saaspe_application_onboarding WHERE work_group IN ('Approver','Reviewer') AND approved_rejected = 'Review' ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC, CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC", nativeQuery = true)
	List<ApplicationOnboarding> findAllSuperAdminListView();

	@Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ApplicationOnboarding c WHERE c.applicationName =:applicationName ")
	boolean findByApplicationName(String applicationName);

	ApplicationOnboarding findByRequestNumber(String requestId);

	ApplicationOnboarding findByChildRequestNumber(String childRequestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	ApplicationOnboarding findAllByReject(String requestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	ApplicationOnboarding findAllByChildReqReject(String childRequestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('superadmin','Approver') and a.approvedRejected ='Approve'")
	ApplicationOnboarding findChildReqSuperApprovee(String childRequestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('superadmin','Approver') and a.approvedRejected ='Approve' ")
	ApplicationOnboarding findReqSuperApprove(String requestId);

	@Query(value = "select * from saaspe_application_onboarding where application_name =:onboardingRequestName and owner_department =:department order by created_on desc limit 1 ;", nativeQuery = true)
	ApplicationOnboarding getDataByApplicationName(String onboardingRequestName, String department);

	@Query("SELECT a FROM ApplicationOnboarding a where a.requestNumber=:requestId")
	List<ApplicationOnboarding> getByRequestNumber(String requestId);

	@Query("SELECT a FROM ApplicationOnboarding a where a.childRequestNumber=:childRequestId")
	List<ApplicationOnboarding> getByChildRequestNumber(String childRequestId);

	@Query(value = "SELECT * FROM saaspe_application_onboarding a where a.application_name = :applicationName  and a.owner_department =:department and a.project_name = :projectName order by a.created_on desc limit 1", nativeQuery = true)
	ApplicationOnboarding findByAppNameandDeptNameandProjectName(String applicationName, String department,
			String projectName);

	@Query("SELECT a FROM ApplicationOnboarding a WHERE a.applicationName = :applicationName AND a.ownerDepartment = :ownerDepartment")
	List<ApplicationOnboarding> findByApplicationNameAndOwnerDept(String applicationName, String ownerDepartment);
	
	@Query(value="select * from  saaspe_application_onboarding where child_request_number =:childRequestId or request_number =:requestId order by created_on desc limit 1",nativeQuery=true)
	ApplicationOnboarding findByRequest(String requestId,String childRequestId);
}

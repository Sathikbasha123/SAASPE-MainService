package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ProjectOnboardingDetails;

public interface ProjectOnboardingDetailsRepository extends JpaRepository<ProjectOnboardingDetails, Integer> {

	@Query(value = "SELECT * FROM SAASPE_PROJECT_ONBOARDING where project_name =:name order by created_on desc limit 1", nativeQuery = true)
	ProjectOnboardingDetails findAllByProjectName(String name);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.requestNumber=:requestId and a.workGroup =:role and a.approvedRejected =:status")
	ProjectOnboardingDetails findByRequestNumber(String requestId, String role, String status);

	@Query(value = "SELECT * FROM saaspe_project_onboarding a where a.work_group =:userRole and a.approved_rejected =:approve ORDER BY CAST(REPLACE(SUBSTRING(request_number, 6), '_', '') AS INTEGER) ASC,CAST(REPLACE(SUBSTRING(child_request_number, 6), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<ProjectOnboardingDetails> getAllByName(String userRole, String approve);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.childRequestNumber=:childRequestId and a.workGroup =:role and a.approvedRejected =:status")
	ProjectOnboardingDetails findByChildRequestNumber(String childRequestId, String role, String status);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	ProjectOnboardingDetails findAllBySuperAdmin(String childRequestId);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	ProjectOnboardingDetails findAllBySuperAdminRequestId(String requestId);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	ProjectOnboardingDetails findAllByChildReqReject(String childRequestId);

	@Query("SELECT a FROM ProjectOnboardingDetails a where a.childRequestNumber=:childRequestId and a.workGroup IN ('superadmin','Approver') and a.approvedRejected ='Approve'")
	ProjectOnboardingDetails findChildReqSuperApprovee(String childRequestId);

	@Query(value = "SELECT * FROM saaspe_project_onboarding a where a.work_group IN ('Approver','Reviewer') and a.approved_rejected ='Review' ORDER BY CAST(REPLACE(SUBSTRING(request_number, 6), '_', '') AS INTEGER) ASC, CAST(REPLACE(SUBSTRING(child_request_number, 6), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<ProjectOnboardingDetails> findAllBySuperAdminListView();

	@Query(value = "SELECT * FROM SAASPE_PROJECT_ONBOARDING  where project_name =:name  order by created_on desc limit 1", nativeQuery = true)
	ProjectOnboardingDetails getProjectStatus(String name);
	
	@Query(value="select * from  saaspe_project_onboarding where child_request_number =:childRequestId or request_number =:requestId order by created_on desc limit 1",nativeQuery=true)
	ProjectOnboardingDetails findByRequest(String requestId,String childRequestId);

}

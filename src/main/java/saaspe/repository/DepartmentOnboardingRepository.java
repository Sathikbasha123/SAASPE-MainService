package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.DepartmentOnboarding;

public interface DepartmentOnboardingRepository extends JpaRepository<DepartmentOnboarding, Integer> {

	DepartmentOnboarding findByRequestNumber(String requestId);

	DepartmentOnboarding findByChildRequestNumber(String childRequestNumber);

	@Query("SELECT a FROM DepartmentOnboarding a where a.requestNumber=:requestId and a.workGroup =:role and a.approvedRejected =:status")
	DepartmentOnboarding findByRequestNumber(String requestId, String role, String status);

	@Query(value = "SELECT * FROM saaspe_department_onboarding a where a.work_group =:userRole and a.approved_rejected =:approve ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC,CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<DepartmentOnboarding> getAllByName(String userRole, String approve);

	@Query("SELECT a FROM DepartmentOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup =:role and a.approvedRejected =:status")
	DepartmentOnboarding findByChildRequestNumber(String childRequestId, String role, String status);

	@Query("SELECT a FROM DepartmentOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	DepartmentOnboarding findAllBySuperAdmin(String childRequestId);

	@Query("SELECT a FROM DepartmentOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer') and a.approvedRejected ='Review'")
	DepartmentOnboarding findAllBySuperAdminRequestId(String requestId);

	@Query("SELECT a FROM DepartmentOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	DepartmentOnboarding findAllByReject(String requestId);

	@Query("SELECT a FROM DepartmentOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('Approver','Reviewer','superadmin') and a.approvedRejected ='Rejected'")
	DepartmentOnboarding findAllByChildReqReject(String childRequestId);

	@Query("SELECT a FROM DepartmentOnboarding a where a.childRequestNumber=:childRequestId and a.workGroup IN ('superadmin','Approver') and a.approvedRejected ='Approve'")
	DepartmentOnboarding findChildReqSuperApprovee(String childRequestId);

	@Query("SELECT a FROM DepartmentOnboarding a where a.requestNumber=:requestId and a.workGroup IN ('superadmin','Approver','super_admin') and a.approvedRejected ='Approve' ")
	DepartmentOnboarding findReqSuperApprove(String requestId);

	@Query(value = "SELECT * FROM saaspe_department_onboarding a where a.work_group IN ('Approver','Reviewer') and a.approved_rejected ='Review' ORDER BY CAST(REPLACE(SUBSTRING(request_number, 9), '_', '') AS INTEGER) ASC, CAST(REPLACE(SUBSTRING(child_request_number, 9), '_', '') AS INTEGER) ASC;", nativeQuery = true)
	List<DepartmentOnboarding> findAllBySuperAdminListView();

	@Query("SELECT a FROM DepartmentOnboarding a where a.departmentName =:departmentName")
	List<DepartmentOnboarding> findByDepartmentName(String departmentName);

	@Query(value = "select * from saaspe_department_onboarding  a where a.department_owner_email =:emailAddress and a.approved_rejected =:approvedOrRejected order by a.created_on desc limit 1;", nativeQuery = true)
	DepartmentOnboarding findByOwnerEmailDAndApprovedReject(String emailAddress, String approvedOrRejected);

	@Query(value="select * from  saaspe_department_onboarding where child_request_number =:childRequestId or request_number =:requestId order by created_on desc limit 1",nativeQuery=true)
	DepartmentOnboarding findByRequest(String requestId, String childRequestId);

}

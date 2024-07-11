package saaspe.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ApplicationDetails;

public interface ApplicationDetailsRepository extends JpaRepository<ApplicationDetails, String> {

	@Query("SELECT a FROM ApplicationDetails a where a.applicationId =:applicationId and a.endDate is null")
	ApplicationDetails findByApplicationId(String applicationId);

	@Query("SELECT a FROM ApplicationDetails a where a.ownerEmail =:ownerEmail and a.endDate is null and a.activeContracts is not null")
	List<ApplicationDetails> findByApplicationOwnerEmail(String ownerEmail);

	@Query(value = "SELECT * FROM public.saaspe_application_details where end_date is null and active_contracts is not null ORDER BY created_on DESC LIMIT 40;", nativeQuery = true)
	List<ApplicationDetails> getDataDesc();

	@Query("select a from ApplicationDetails a where a.categoryId = :categoryId")
	List<ApplicationDetails> findByCategoryId(String categoryId);

	@Query(value = "SELECT * FROM saaspe_application_details a where a.application_name = :applicationName and a.owner_department =:department and a.end_date is null order by a.created_on desc limit 1", nativeQuery = true)
	ApplicationDetails getDeletedApplicationByAppName(String applicationName, String department);

	@Query("SELECT a FROM ApplicationDetails a where a.endDate is null and a.activeContracts is not null")
	List<ApplicationDetails> findRemainingApplications();

	@Query(value = "SELECT * FROM saaspe_application_details a where a.application_name = :applicationName and a.owner_department =:department and a.project_name =:projectName and a.end_date is null order by a.created_on desc limit 1", nativeQuery = true)
	ApplicationDetails getDeletedApplicationByAppNameAndProjectName(String applicationName, String department,
			String projectName);

	@Query(value = "select * from saaspe_application_details a where a.graph_application_id is not null and a.end_date is null ", nativeQuery = true)
	List<ApplicationDetails> getGraphApplicationId();

	@Query("SELECT a FROM ApplicationDetails a where a.applicationName =:applicationName and a.endDate is null")
	List<ApplicationDetails> findByApplicationName(String applicationName);

	@Query("SELECT a FROM ApplicationDetails a where a.ownerDepartment =:deptName ")
	List<ApplicationDetails> findByDepartmentName(String deptName);

	@Query("SELECT a FROM ApplicationDetails a where a.activeContracts is not null AND a.createdOn BETWEEN :start AND :end and a.endDate is null")
	List<ApplicationDetails> getAllApplicationBtwDates(Date start, Date end);

	@Query(value = "select * from saaspe_application_details a where a.project_name =:projectName and a.active_contracts is not null and a.end_date is null ", nativeQuery = true)
	List<ApplicationDetails> findByProejctName(String projectName);

	@Query("SELECT a FROM ApplicationDetails a where a.endDate is null")
	List<ApplicationDetails> findNotDeletedApplications();

	@Query("SELECT a FROM ApplicationDetails a WHERE a.applicationName = :applicationName AND a.ownerDepartment = :ownerDepartment AND a.endDate IS NULL")
	List<ApplicationDetails> findByApplicationNameAndOwnerDept(String applicationName, String ownerDepartment);

	@Query(value = "SELECT * FROM saaspe_application_details a where a.application_id =:id",nativeQuery = true)
	ApplicationDetails findByAppId(String id);
}
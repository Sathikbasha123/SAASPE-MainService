package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ProjectManagerDetails;

public interface ProjectOwnerRepository extends JpaRepository<ProjectManagerDetails, Integer> {

	@Query("SELECT a FROM ProjectManagerDetails a WHERE a.projectId =:projectId")
	List<ProjectManagerDetails> findByProjectId(String projectId);

	@Query("SELECT a FROM ProjectManagerDetails a WHERE a.projectId =:projectId and a.endDate is null")
	List<ProjectManagerDetails> findByProjectIdandEndDate(String projectId);

	@Query("SELECT a FROM ProjectManagerDetails a WHERE a.projectManagerEmail =:projectManagerEmail")
	List<ProjectManagerDetails> findByProjectByEmail(String projectManagerEmail);
	
	@Query(value = "SELECT DISTINCT ON (project_manager_email) * FROM saaspe_project_manager_details a WHERE a.project_id = :projectId ORDER BY project_manager_email,end_date DESC",nativeQuery = true)
	List<ProjectManagerDetails> findByProjectIdOrderByEndDate(String projectId);
}

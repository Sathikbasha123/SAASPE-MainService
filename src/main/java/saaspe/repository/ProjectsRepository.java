package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.Projects;

public interface ProjectsRepository extends JpaRepository<Projects, Integer> {
	
	@Query(value="SELECT * FROM saaspe_projects WHERE project_id = :projectId AND application_id IS NULL",nativeQuery = true)
	List<Projects> findByProjectId(String projectId);
	
	@Query(value="SELECT * FROM saaspe_projects WHERE project_manager_email = :email AND project_id = :projectId",nativeQuery = true)
	List<Projects> findByOwnerEmail(String email, String projectId);
	
}

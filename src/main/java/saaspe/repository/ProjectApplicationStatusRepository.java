package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ProjectApplicationStatus;

public interface ProjectApplicationStatusRepository extends JpaRepository<ProjectApplicationStatus, Integer> {

	@Query("SELECT a FROM ProjectApplicationStatus a where a.projectId =:projectId")
	List<ProjectApplicationStatus> findByProjectId(String projectId);
}

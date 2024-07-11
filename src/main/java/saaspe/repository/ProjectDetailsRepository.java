package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.ProjectDetails;

public interface ProjectDetailsRepository extends JpaRepository<ProjectDetails, String> {

	@Query(value = "select * from saaspe_department_project_details where project_name =:projectName ;", nativeQuery = true)
	ProjectDetails findByProjectName(String projectName);

	@Query(value = "select * from saaspe_department_project_details where project_name =:projectName ;", nativeQuery = true)
	ProjectDetails findByProjName(String projectName);

	@Query(value = "select * from saaspe_department_project_details where department_id =:deptId ;", nativeQuery = true)
	List<ProjectDetails> getProjectsByDeptId(String deptId);

	@Query(value = "select * from saaspe_department_project_details where project_id =:projectId ;", nativeQuery = true)
	ProjectDetails findByProjectId(String projectId);

	@Query(value = "select * from saaspe_department_project_details where project_name =:projectName and department_id=:departmentId ;", nativeQuery = true)
	ProjectDetails findByProjectIdAndDeptId(String projectName, String departmentId);

	@Query(value = "select * from saaspe_department_project_details ;", nativeQuery = true)
	List<ProjectDetails> getProjectDetails();

	@Query(value = "select * from saaspe_department_project_details where project_manager =:email ;", nativeQuery = true)
	List<ProjectDetails> getProjectByEmail(String email);

	@Query(value = "select project_name from saaspe_department_project_details pr join department_details d on d.department_id= pr.department_id where d.department_name= :departmentName ;", nativeQuery = true)
	List<String> findProjectByDeptName(String departmentName);
}

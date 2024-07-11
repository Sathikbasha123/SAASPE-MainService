package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.Departments;

public interface DepartmentsRepository extends JpaRepository<Departments, Integer> {

	@Query(value = "select * from saaspe_departments where department_id =:departmentId ;", nativeQuery = true)
	Departments findByDepartmentId(String departmentId);

	 @Query(value = "select * from saaspe_departments where department_name = :departmentName", nativeQuery = true)
	  List<Departments> findByDepartmentName(String departmentName);


	@Query(value = "select * from saaspe_departments where application_id =:applicationId ;", nativeQuery = true)
	List<Departments> findByApplicationId(String applicationId);

	@Query(value = "select * from saaspe_departments where department_id =:departmentId ;", nativeQuery = true)
	List<Departments> findByDepartmentIds(String departmentId);
 
}

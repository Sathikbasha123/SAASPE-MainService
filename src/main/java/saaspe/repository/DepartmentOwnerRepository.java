package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.DepartmentOwnerDetails;

public interface DepartmentOwnerRepository extends JpaRepository<DepartmentOwnerDetails, Integer> {

	@Query(value = "SELECT a FROM DepartmentOwnerDetails a WHERE a.deptId =:id ")
	List<DepartmentOwnerDetails> findByDepartmentId(String id);

	@Query(value = "SELECT a FROM DepartmentOwnerDetails a WHERE a.deptId =:id and a.endDate is null")
	List<DepartmentOwnerDetails> findByDepartmentIdAndEnddate(String id);

	@Query(value = "SELECT a FROM DepartmentOwnerDetails a WHERE a.departmentName =:departmentName ")
	List<DepartmentOwnerDetails> findByDepartmentName(String departmentName);

	@Query(value = "SELECT a FROM DepartmentOwnerDetails a WHERE a.departmentOwnerEmail =:userEmail ")
	List<DepartmentOwnerDetails> findByDepartmentOwnerEmail(String userEmail);

}

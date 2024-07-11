package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.DepartmentDetails;

public interface DepartmentRepository extends JpaRepository<DepartmentDetails, String> {

    @Query(value = "select * from department_details where department_id =:id ;", nativeQuery = true)
    DepartmentDetails findByDepartmentId(String id);

    @Query(value = "select * from department_details where department_name =:deptName ;", nativeQuery = true)
    DepartmentDetails findByDepartmentName(String deptName);

    DepartmentDetails findByApplicationId(String applicationId);

    @Query(value = "select * from department_details where department_admin =:emailAddress ;", nativeQuery = true)
    DepartmentDetails findByDeptOwnerEmail(String emailAddress);
    
   

}

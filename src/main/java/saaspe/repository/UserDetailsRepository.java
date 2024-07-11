package saaspe.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.dto.MetricsDAO;
import saaspe.entity.UserDetails;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, String> {

	// @Query("Select a from UserDetails a where a.userEmail = :userEmail")

	@Query(value = "Select * from saaspe_user_details where user_email =:userEmail and end_date is null ;", nativeQuery = true)
	UserDetails findByuserEmail(String userEmail);

	@Query(value = "SELECT u FROM UserDetails u WHERE u.userEmail IN :departmentOwnerEmailAddress")
	List<UserDetails> findByDepartmentOwnerEmailAddressIn(List<String> departmentOwnerEmailAddress);

	@Query(value = "Select * from saaspe_user_details where user_email =:userEmail and end_date is null and user_role  in ('SUPER_ADMIN','REVIEWER','APPROVER') ;", nativeQuery = true)
	UserDetails findByuserEmailAndRole(String userEmail);

	@Query(value = "Select * from saaspe_user_details where user_email =:userEmail and end_date is null and department_id is not null and user_role ='CONTRIBUTOR';", nativeQuery = true)
	UserDetails findByuserEmailAndDepartment(String userEmail);

	@Query("Select a from UserDetails a where a.userEmail = :userEmail")
	UserDetails getByEmail(String userEmail);

	@Query(value = "select * from (select count(a.user_email) as count,a.license_id as licenseId from saaspe_user_details a group by a.license_id )\r\n"
			+ "a inner join\r\n"
			+ "saaspe_application_license_details b on a.licenseid = b.license_id ORDER BY count DESC limit 10", nativeQuery = true)
	List<MetricsDAO> getTopAppsByUsercount();

	@Query(value = "select * from (select count(a.user_email) as count,a.license_id as licenseId from saaspe_user_details a group by a.license_id ) a inner join \r\n"
			+ "saaspe_application_license_details b on a.licenseid = b.license_id where b.application_id = :applicationId ;", nativeQuery = true)
	MetricsDAO groupByApplicationID(String applicationId);

	@Query(value = "select a.department_id as departmentId,count(a.user_email) from saaspe_user_details a where a.end_date is null group by a.department_id having a.department_id = :departmentId ;", nativeQuery = true)
	MetricsDAO findByDeparmentId(String departmentId);

	@Query(value = "select * from saaspe_user_details where user_id = :userId ;", nativeQuery = true)
	UserDetails findByUserId(String userId);

	@Query(value = "select * from saaspe_user_details where department_id = :departmentId and end_date is null ;", nativeQuery = true)
	List<UserDetails> getAllUsersByDepartmentId(String departmentId);

	@Query(value = "Select * from(select * from saaspe_user_details where user_role NOT in ('APPROVER','REVIEWER','SUPER_ADMIN') AND department_id is NOT null OR user_role is NULL) a where a.end_date is null", nativeQuery = true)
	List<UserDetails> getByRolesExclude();

	@Query(value = "select * from saaspe_user_details a where a.user_email =:userEmail and a.end_date is null order by a.created_on desc limit 1", nativeQuery = true)
	UserDetails getDeletedUserByUserEmail(String userEmail);

	@Query(value = "SELECT * FROM saaspe_user_details a where a.department_id = :deptartmentId and a.user_email =:userEmail and a.end_date is null", nativeQuery = true)
	UserDetails findByDepartmentIdAndUserEmail(String deptartmentId, String userEmail);

	@Query(value = "SELECT * FROM saaspe_user_details a where a.user_email = :userEmail and a.end_date is null order by a.created_on desc limit 1", nativeQuery = true)
	UserDetails getDeleteduserEmail(String userEmail);

	@Query(value = "select * from saaspe_user_details where user_role is not null and end_date is null", nativeQuery = true)
	List<UserDetails> getAllAdmins();

	@Query(value = "select * from saaspe_user_details where identity_id is null", nativeQuery = true)
	List<UserDetails> findAllByGraphUserId();

	@Query(value = "select * from saaspe_user_details where identity_id is not null", nativeQuery = true)
	List<UserDetails> findByGraphIdentityId();

	@Query(value = "SELECT * FROM saaspe_user_details a WHERE a.user_role = 'SUPER_ADMIN' AND a.currency IS NOT NULL AND a.user_id IS NULL ORDER BY a.created_on DESC LIMIT 1", nativeQuery = true)
	UserDetails getCurrency();

	@Modifying
	@Transactional
	@Query(value = "delete from users_application a where a.user_email =:userEmail and a.application_id =:applicationId", nativeQuery = true)
	void removeUserEmail(String userEmail, String applicationId);

	@Query(value = "select * from saaspe_user_details a where a.user_role ='SUPER_ADMIN' and end_date is null and a.user_id is null ;", nativeQuery = true)
	List<UserDetails> findAllSuperAdmin();

	@Query(value = "select * from saaspe_user_details a where a.mobile_number =:userMobileNumber and end_date is null ;", nativeQuery = true)
	UserDetails findByUserMobileNumber(String userMobileNumber);

	@Query(value = "Select user_email from(select * from saaspe_user_details where user_role NOT in ('APPROVER','REVIEWER','SUPER_ADMIN') AND department_id is NOT null OR user_role is NULL) a where a.end_date is null", nativeQuery = true)
	List<String> getByRolesExcludeUser();

	@Query(nativeQuery = true, value = "select user_email from saaspe_user_details where application_id=:applicationId")
	List<String> findByApplicationId(String applicationId);
}

package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saaspe.entity.UserDetails;
import saaspe.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {
	
	@Query("Select a from Users a where a.userEmail = :userEmail")
	Users getByEmail(String userEmail);

	@Query(value = "Select * from saaspe_users where user_email =:userEmail and user_end_date is null ;", nativeQuery = true)
	Users findByuserEmail(String userEmail);
	
	@Query(value = "select * from saaspe_users where user_id = :userId ;", nativeQuery = true)
	Users findByUserId(String userId);

	@Query(value = "select * from saaspe_users where user_application_id = :userApplicationId ;", nativeQuery = true)
	List<Users> findByUserApplicationId(String userApplicationId);


}

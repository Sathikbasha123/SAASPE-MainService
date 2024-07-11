package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saaspe.entity.UserLoginDetails;

@Repository
public interface UserLoginDetailsRepository extends JpaRepository<UserLoginDetails, String> {

    @Query("Select a from UserLoginDetails a where a.emailAddress=:emailAddress")
    List<UserLoginDetails> getUserProfile(@Param("emailAddress") String emailAddress);

    @Query("Select a from UserLoginDetails a where (a.emailAddress=:emailAddress)")
    UserLoginDetails findByUserEmail(@Param("emailAddress") String emailAddress);

    @Query("Select a from UserLoginDetails a where LOWER(a.emailAddress)=:emailAddress")
    UserLoginDetails findByEmail(@Param("emailAddress") String emailAddress);


}
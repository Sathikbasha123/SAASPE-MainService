package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import saaspe.entity.UserActions;

@Repository
public interface UserActionsRepository extends JpaRepository<UserActions, Long> {

	@Query("Select a from UserActions a where a.traceId = :traceId")
	UserActions findActionsBytraceId(String traceId);

	@Query("Select a from UserActions a where a.userEmail = :emailAddress")
	List<UserActions> findByUserEmail(String emailAddress);

}

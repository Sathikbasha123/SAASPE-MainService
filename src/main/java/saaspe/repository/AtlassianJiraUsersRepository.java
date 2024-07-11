package saaspe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import saaspe.entity.AtlassianJiraUsers;

public interface AtlassianJiraUsersRepository extends JpaRepository<AtlassianJiraUsers, String> {

	AtlassianJiraUsers findByUserEmail(String userEmail);
	
	List<AtlassianJiraUsers> findAll();
}

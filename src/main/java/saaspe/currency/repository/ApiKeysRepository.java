package saaspe.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.currency.entity.ApiKeys;

public interface ApiKeysRepository extends JpaRepository<ApiKeys, Long> {

	@Query("Select a from ApiKeys a where a.id = 1 ")
	ApiKeys getById();

}

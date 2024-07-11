package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.AuthenticationEntity;

public interface AuthenticaionServiceRepository extends JpaRepository<AuthenticationEntity, Integer> {

    @Query("select a from AuthenticationEntity a where a.ssoIdentityProvider =:applicationName")
    AuthenticationEntity findBySsoIdentityProvider(String applicationName);

}

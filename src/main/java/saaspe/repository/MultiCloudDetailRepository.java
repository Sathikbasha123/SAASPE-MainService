package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import saaspe.entity.MultiCloudDetails;

public interface MultiCloudDetailRepository extends JpaRepository<MultiCloudDetails,String> {

    @Query("Select a from MultiCloudDetails a where a.providerName =:provider")
    MultiCloudDetails findByProviderName(String provider);

    @Query("Select a from MultiCloudDetails a where a.cloudId =:cloudId")
	MultiCloudDetails findByCloudId(String cloudId);
}

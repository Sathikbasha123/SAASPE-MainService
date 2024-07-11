package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.ResourcesDocument;

public interface ResourcesRepository extends MongoRepository<ResourcesDocument, Long> {

    @Query("{ 'resourceGroupId' : :#{#resourceGroupId} , 'subscriptionId' : :#{#subscriptionId}}")
    List<ResourcesDocument> getresoucresByResourcesGroup(String resourceGroupId,String subscriptionId);

    @Query("{'subscriptionId' : :#{#subscriptionId}}")
    List<ResourcesDocument> findBySubscriptionId(String subscriptionId);

}

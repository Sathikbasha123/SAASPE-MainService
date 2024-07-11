package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.ResourceGroupDocument;

public interface ResourceGroupRepository extends MongoRepository<ResourceGroupDocument, Long> {

    @Query("{'subscriptionId' : :#{#subscriptionId}}")
    List<ResourceGroupDocument> getResourceBySubscriptionId(String subscriptionId);

}

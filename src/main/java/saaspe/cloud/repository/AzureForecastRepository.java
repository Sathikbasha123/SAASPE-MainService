package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import saaspe.document.AzureForecastDocument;

public interface AzureForecastRepository extends MongoRepository<AzureForecastDocument, Long> {

    @Query("{'subscriptionId' : ?0}")
    AzureForecastDocument findBySubscriptionId(String susbcriptionid);

    @Query("{'subscriptionId' : :#{#subscriptionId}}")
    List<AzureForecastDocument> getBySubscriptionId(String subscriptionId);

}

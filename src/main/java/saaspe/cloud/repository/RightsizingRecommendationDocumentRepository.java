package saaspe.cloud.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.AwsRightsizingRecommendationDocument;

public interface RightsizingRecommendationDocumentRepository
        extends MongoRepository<AwsRightsizingRecommendationDocument, Long> {

    @Query("{'accountId' : :#{#accountId}}")
    List<AwsRightsizingRecommendationDocument> getByAccountId(String accountId);

}

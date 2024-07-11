package saaspe.marketplace.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.ProductReviewsDocument;

public interface ProductReviewsRepository extends MongoRepository<ProductReviewsDocument, String> {

    @Query("{'vendorId' : :#{#uUID}}")
    List<ProductReviewsDocument> findByUUID(String uUID);

}

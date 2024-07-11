package saaspe.marketplace.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import saaspe.document.ProductItemsDocumet;

public interface ProductItemsDocumetRepository extends MongoRepository<ProductItemsDocumet, String> {

    @Query("{'UUID' : :#{#uUID}}")
    ProductItemsDocumet getProductItemsDocumetById(String uUID);

}

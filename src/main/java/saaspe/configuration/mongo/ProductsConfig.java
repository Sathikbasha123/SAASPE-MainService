package saaspe.configuration.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {
        "saaspe.marketplace.repository" }, mongoTemplateRef = ProductsConfig.MONGO_TEMPLATE)
public class ProductsConfig {

    protected static final String MONGO_TEMPLATE = "ProductsMongoTemplate";
}

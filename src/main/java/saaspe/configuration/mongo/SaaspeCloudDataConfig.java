package saaspe.configuration.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {
        "saaspe.cloud.repository" }, mongoTemplateRef = SaaspeCloudDataConfig.MONGO_TEMPLATE)
public class SaaspeCloudDataConfig {
    protected static final String MONGO_TEMPLATE = "SaaspeCloudDataMongoTemplate";
}

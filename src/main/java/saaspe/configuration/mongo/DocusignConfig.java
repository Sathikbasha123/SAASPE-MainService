package saaspe.configuration.mongo;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {
		
		

        "saaspe.docusign.repository" }, mongoTemplateRef = DocusignConfig.MONGO_TEMPLATE)
     public class DocusignConfig {

	protected static final String MONGO_TEMPLATE = "DocusigMongoTemplate";
}

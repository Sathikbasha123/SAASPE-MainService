package saaspe.configuration.mongo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import saaspe.constant.Constant;

@Configuration
public class MultipleMongoConfig {

	@Value("${spring.data.mongodb.marketplace.host}")
	private String marketPlaceHost;

	@Value("${spring.data.mongodb.marketplace.port}")
	private int marketPlacePort;

	@Value("${spring.data.mongodb.marketplace.database}")
	private String marketPlaceDataBaseName;

	@Value("${spring.data.mongodb.marketplace.username}")
	private String marketPlaceUserName;

	@Value("${spring.data.mongodb.marketplace.password}")
	private String marketPlacePassword;

	@Value("${spring.data.mongodb.cloud.host}")
	private String cloudDataHost;

	@Value("${spring.data.mongodb.cloud.port}")
	private int cloudDataPort;

	@Value("${spring.data.mongodb.cloud.database}")
	private String cloudDataDataBaseName;

	@Value("${spring.data.mongodb.cloud.username}")
	private String cloudDataUserName;

	@Value("${spring.data.mongodb.cloud.password}")
	private String cloudDataPaasowrd;

	@Value("${spring.data.mongodb.docusign.host}")
	private String docusignHost;

	@Value("${spring.data.mongodb.docusign.port}")
	private int docusignPort;

	@Value("${spring.data.mongodb.docusign.database}")
	private String docusignDataBaseName;

	@Value("${spring.data.mongodb.docusign.username}")
	private String docusignUserName;

	@Value("${spring.data.mongodb.docusign.password}")
	private String docusignPaasowrd;

	@Primary
	@Bean(name = "ProductsProperties")
	@ConfigurationProperties(prefix = "spring.data.mongodb.marketplace")
	public MongoProperties getProductsProperties() {
		return new MongoProperties();
	}

	@Bean(name = "CloudDataProperties")
	@ConfigurationProperties(prefix = "spring.data.mongodb.cloud")
	public MongoProperties getSaaspeCloudDataProperties() {
		return new MongoProperties();
	}

	@Bean(name = "DocusginProperties")
	@ConfigurationProperties(prefix = "spring.data.mongodb.docusign")
	public MongoProperties getDocusignProperties() {
		return new MongoProperties();
	}

	@Primary
	@Bean(name = "ProductsMongoTemplate")
	public MongoTemplate productsMongoTemplate() {
		return new MongoTemplate(productsMongoDatabaseFactory(getProductsProperties()));
	}

	@Bean(name = "SaaspeCloudDataMongoTemplate")
	public MongoTemplate cloudDataMongoTemplate() {
		return new MongoTemplate(cloudDataMongoDatabaseFactory(getSaaspeCloudDataProperties()));
	}

	@Bean(name = "DocusigMongoTemplate")
	public MongoTemplate docusignMongoTemplate() {
		return new MongoTemplate(docusginMongoDatabaseFactory(getDocusignProperties()));
	}

	@Primary
	@Bean
	public MongoDatabaseFactory productsMongoDatabaseFactory(MongoProperties mongo) {
		String prefix = Constant.MONGODB;
		String url = prefix + marketPlaceUserName + ":" + marketPlacePassword + "@" + marketPlaceHost + ":"
				+ marketPlacePort + "/" + marketPlaceDataBaseName + Constant.AUTHMECHANISM;
		return new SimpleMongoClientDatabaseFactory(url);
	}

	@Bean
	public MongoDatabaseFactory cloudDataMongoDatabaseFactory(MongoProperties mongo) {
		String prefix = Constant.MONGODB;
		String url = prefix + cloudDataUserName + ":" + cloudDataPaasowrd + "@" + cloudDataHost + ":" + cloudDataPort
				+ "/" + cloudDataDataBaseName + Constant.AUTHMECHANISM;
		return new SimpleMongoClientDatabaseFactory(url);
	}

	@Bean
	public MongoDatabaseFactory docusginMongoDatabaseFactory(MongoProperties mongo) {
		String prefix = Constant.MONGODB;
		String url = prefix + docusignUserName + ":" + docusignPaasowrd + "@" + docusignHost + ":" + docusignPort + "/"
				+ docusignDataBaseName + Constant.AUTHMECHANISM;
		return new SimpleMongoClientDatabaseFactory(url);
	}

}

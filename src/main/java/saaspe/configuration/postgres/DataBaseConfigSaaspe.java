package saaspe.configuration.postgres;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "saaspeEntityManagerFactory", transactionManagerRef = "saaspeTransactionManager", basePackages = {
		"saaspe.repository" })
public class DataBaseConfigSaaspe {

	@Value("${spring.datasource.username}")
	private String saaspeUserName;

	@Value("${spring.datasource.password}")
	private String saaspePassword;

	@Value("${spring.datasource.url}")
	private String saaspeUrl;

	@Value("${spring.datasource.hikari.connectionTimeout}")
	private String connectionTimeout;

	@Value("${spring.datasource.hikari.minimumIdle}")
	private String minimumIdle;

	@Value("${spring.datasource.hikari.maximumPoolSize}")
	private String maximumPoolSize;

	@Value("${spring.datasource.hikari.idleTimeout}")
	private String idleTimeout;

	@Value("${spring.datasource.hikari.maxLifetime}")
	private String maxLifetime;

	@Value("${spring.datasource.hikari.autoCommit}")
	private String autoCommit;

	@Primary
	@Bean(name = "spring.datasource")
	@ConfigurationProperties(prefix = "spring.datasource.url")
	public DataSource dataSource() {
		return DataSourceBuilder.create().url(saaspeUrl).username(saaspeUserName).password(saaspePassword)
				.type(HikariDataSource.class).build();
	}

	@Primary
	@Bean(name = "saaspeEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean saaspeEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("spring.datasource") DataSource dataSource) {
		return builder.dataSource(dataSource).packages("saaspe.entity").properties(hikariProperties()).build();
	}

	private Map<String, Object> hikariProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("spring.datasource.hikari.connectionTimeout", connectionTimeout);
		properties.put("spring.datasource.hikari.minimumIdle", minimumIdle);
		properties.put("spring.datasource.hikari.maximumPoolSize", maximumPoolSize);
		properties.put("spring.datasource.hikari.idleTimeout", idleTimeout);
		properties.put("spring.datasource.hikari.maxLifetime", maxLifetime);
		properties.put("spring.datasource.hikari.autoCommit", autoCommit);
		return properties;
	}

	@Primary
	@Bean(name = "saaspeTransactionManager")
	public PlatformTransactionManager saaspeTransactionManager(
			@Qualifier("saaspeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

}

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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "currencyEntityManagerFactory", transactionManagerRef = "currencyTransactionManager", basePackages = {
		"saaspe.currency.repository" })
public class DatabaseConfigCurrency {

	@Value("${currency.datasource.username}")
	private String currencyUserName;

	@Value("${currency.datasource.password}")
	private String currencyPassword;

	@Value("${currency.datasource.url}")
	private String currencyUrl;

	@Value("${currency.datasource.hikari.connectionTimeout}")
	private String connectionTimeout;

	@Value("${currency.datasource.hikari.minimumIdle}")
	private String minimumIdle;

	@Value("${currency.datasource.hikari.maximumPoolSize}")
	private String maximumPoolSize;

	@Value("${currency.datasource.hikari.idleTimeout}")
	private String idleTimeout;

	@Value("${currency.datasource.hikari.maxLifetime}")
	private String maxLifetime;

	@Value("${currency.datasource.hikari.auto-commit}")
	private String autoCommit;

	@Bean(name = "currencyDatasource")
	@ConfigurationProperties(prefix = "currency.datasource.url")
	public DataSource dataSource() {
		return DataSourceBuilder.create().url(currencyUrl).username(currencyUserName).password(currencyPassword)
				.type(HikariDataSource.class).build();
	}

	@Bean(name = "currencyEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean currencyEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("currencyDatasource") DataSource dataSource) {
		return builder.dataSource(dataSource).packages("saaspe.currency.entity").properties(hikariProperties()).build();
	}

	private Map<String, Object> hikariProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("currency.datasource.hikari.connectionTimeout", connectionTimeout);
		properties.put("currency.datasource.hikari.minimumIdle", minimumIdle);
		properties.put("currency.datasource.hikari.maximumPoolSize", maximumPoolSize);
		properties.put("currency.datasource.hikari.idleTimeout", idleTimeout);
		properties.put("currency.datasource.hikari.maxLifetime", maxLifetime);
		properties.put("currency.datasource.hikari.auto-commit", autoCommit);
		return properties;
	}

	@Bean(name = "currencyTransactionManager")
	public PlatformTransactionManager currencyTransactionManager(
			@Qualifier("currencyEntityManagerFactory") EntityManagerFactory barEntityManagerFactory) {
		return new JpaTransactionManager(barEntityManagerFactory);
	}

}

package uk.gov.dwp.jsa.validation.service.integration;

import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;
import static uk.gov.dwp.jsa.validation.service.integration.ClaimStatisticsIntegrationTest.postgreSQLContainer;

public class DbConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        // here we reference the static test container variable in our test case to get the used the connection details
        ds.setUrl(format("jdbc:postgresql://%s:%s/%s", postgreSQLContainer.getContainerIpAddress(),
                postgreSQLContainer.getMappedPort(
                        PostgreSQLContainer.POSTGRESQL_PORT), postgreSQLContainer.getDatabaseName()));
        ds.setUsername(postgreSQLContainer.getUsername());
        ds.setPassword(postgreSQLContainer.getPassword());
        ds.setSchema(postgreSQLContainer.getDatabaseName());
        return ds;
    }

    /**
     * @param localContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean
     * @return the JPA transaction manager
     */
    @Bean
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();

        transactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean.getObject());

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) throws SQLException {
        Map<String, String> changeLogParams = new HashMap<>();
        changeLogParams.put("super_user", "postgres");
        changeLogParams.put("app_user", "validation_db_user");
        changeLogParams.put("app_password", "mypassword");
        changeLogParams.put("app_schema", "validation_schema");

        tryToCreateSchema(dataSource);
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDropFirst(true);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("test");
        liquibase.setChangeLog("file:devops/liquibase/db/changelog/changelog.sql");
        liquibase.setChangeLogParameters(changeLogParams);

        return liquibase;
    }


    /**
     * @return the hibernate properties
     */
    private Properties getHibernateProperties() {
        Properties ps = new Properties();
        ps.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        ps.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
        ps.put("hibernate.hbm2ddl.auto", "none");
        ps.put("hibernate.connection.characterEncoding", "UTF-8");
        ps.put("hibernate.connection.charSet", "UTF-8");

        ps.put(AvailableSettings.FORMAT_SQL, "true");
        ps.put(AvailableSettings.SHOW_SQL, "true");
        return ps;

    }

    private void tryToCreateSchema(DataSource dataSource) throws SQLException {
        String CREATE_SCHEMA_QUERY = "CREATE SCHEMA IF NOT EXISTS test";
        dataSource.getConnection().createStatement().execute(CREATE_SCHEMA_QUERY);
    }

}

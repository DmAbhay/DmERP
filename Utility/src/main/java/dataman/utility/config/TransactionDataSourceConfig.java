package dataman.utility.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "dataman.utility.userMast.repository",
        entityManagerFactoryRef = "transactionEntityManagerFactory",
        transactionManagerRef = "transactionTransactionManager"
)
public class TransactionDataSourceConfig {

    @Autowired
    private ExternalConfig externalConfig;

    @Bean(name = "transactionDataSource")
    public DataSource transactionDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + externalConfig.getSqlHostName() + ":" + externalConfig.getSqlPort() +
                        ";databaseName=" + "tiranga_Transaction" + ";encrypt=true;trustServerCertificate=true")
                .username(externalConfig.getSqlUser())
                .password(externalConfig.getSqlPassword())
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .build();
    }

    @Bean(name = "transactionEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean transactionEntityManagerFactory(
            @Qualifier("transactionDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("dataman.utility.userMast.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaProperties(jpaProperties());
        return factory;
    }

    @Bean(name = "transactionTransactionManager")
    public JpaTransactionManager transactionTransactionManager(
            @Qualifier("transactionEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        return properties;
    }

    @Bean(name = "transactionJdbcTemplate")
    public JdbcTemplate transactionJdbcTemplate(@Qualifier("transactionDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "transactionNamedJdbcTemplate")
    public NamedParameterJdbcTemplate transactionNamedJdbcTemplate(
            @Qualifier("transactionJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}


//package dataman.utility.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.Properties;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = "dataman.utility.userMast.repository",
//        entityManagerFactoryRef = "companyEntityManagerFactory",
//        transactionManagerRef = "companyTransactionManager"
//)
//public class TransactionDataSourceConfig {
//
//    //companyDlmTransactionManager  companyTirangaDataSource  companyDlmDataSource
//
//
//    @Autowired
//    private ExternalConfig externalConfig;  // Ensure this class is defined and injected correctly here we companyDb name becouse company tabel is here
//
//    @Bean(name = "companyDataSource")
//    DataSource companyDataSource() {
//        return DataSourceBuilder.create()
//                .url("jdbc:sqlserver://" + externalConfig.getSqlHostName() + ":" + externalConfig.getSqlPort() +
//                        ";databaseName=" + "tiranga_Company" + ";encrypt=true;trustServerCertificate=true")
//                .username(externalConfig.getSqlUser())
//                .password(externalConfig.getSqlPassword())
//                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
//                .build();
//
//    }
//
//    @Bean(name = "companyEntityManagerFactory")
//    LocalContainerEntityManagerFactoryBean companyEntityManagerFactory(
//            @Qualifier("companyDataSource") DataSource dataSource) {
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setPackagesToScan("dataman.utility.userMast.entity"); // Adjust to where your entity classes are located
//        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter()); // Set Hibernate as JPA provider
//        factory.setJpaProperties(jpaProperties()); // Set JPA properties
//        return factory;
//    }
//
//    @Bean(name = "companyTransactionManager")
//    JpaTransactionManager companyTirangaTransactionManager(
//            @Qualifier("companyEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory.getObject());
//    }
//
//    // Method to define JPA properties
//    private Properties jpaProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
//        properties.setProperty("hibernate.show_sql", "true");
//        properties.setProperty("hibernate.format_sql", "true");
//        return properties;
//    }
//
//    @Bean(name = "companyJdbcTemplate")
//    JdbcTemplate companyJdbcTemplate(@Qualifier("companyDataSource") DataSource dlmTransactionDataSource) {
//        return new JdbcTemplate(dlmTransactionDataSource);
//    }
//
//    @Bean(name = "companyNamedJdbcTemplate")
//    public NamedParameterJdbcTemplate companyNamedJdbcTemplate(
//            @Qualifier("tirangaCompanyJdbcTemplate") JdbcTemplate jdbcTemplate) {
//        return new NamedParameterJdbcTemplate(jdbcTemplate);
//    }
//
//
//}

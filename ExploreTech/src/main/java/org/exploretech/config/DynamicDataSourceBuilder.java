package org.exploretech.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;


import javax.sql.DataSource;
import java.util.Properties;
//
//@Service
//public class DynamicDataSourceBuilder {
//
//    public DataSource buildDataSource(Properties props) {
//        return org.springframework.boot.jdbc.DataSourceBuilder.create()
//                .url("jdbc:sqlserver://" + props.getProperty("sqlHostName") + ":" + props.getProperty("sqlPort") +
//                        ";databaseName=" + props.getProperty("companyDb") + ";encrypt=true;trustServerCertificate=true")
//                .username(props.getProperty("sqlUser"))
//                .password(props.getProperty("sqlPassword"))
//                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
//                .build();
//    }
//
//    public JdbcTemplate buildJdbcTemplate(DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//
//    public NamedParameterJdbcTemplate buildNamedJdbcTemplate(JdbcTemplate jdbcTemplate) {
//        return new NamedParameterJdbcTemplate(jdbcTemplate);
//    }
//
//    public LocalContainerEntityManagerFactoryBean buildEntityManagerFactory(DataSource dataSource) {
//        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//        factory.setDataSource(dataSource);
//        factory.setPackagesToScan("dlms.loan.singup.entity"); // or make this dynamic
//        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        factory.setJpaProperties(jpaProperties());
//        return factory;
//    }
//
//    private Properties jpaProperties() {
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
//        properties.setProperty("hibernate.show_sql", "true");
//        properties.setProperty("hibernate.format_sql", "true");
//        return properties;
//    }
//}



@Service
public class DynamicDataSourceBuilder {

    public DataSource buildDataSource(Properties props) {
        return DataSourceBuilder.create()
                .url("jdbc:sqlserver://" + props.getProperty("sqlHostName") + ":" + props.getProperty("sqlPort") +
                        ";databaseName=" + props.getProperty("companyDb") + ";encrypt=true;trustServerCertificate=true")
                .username(props.getProperty("sqlUser"))
                .password(props.getProperty("sqlPassword"))
                .driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                .build();
    }

    public JdbcTemplate buildJdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    public NamedParameterJdbcTemplate buildNamedJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}

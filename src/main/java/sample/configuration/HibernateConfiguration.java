package sample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan({"sample"})
@PropertySource("classpath:database.properties")
public class HibernateConfiguration {
    private final Environment environment;

    @Autowired
    public HibernateConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driverClassName"));
        dataSource.setUrl(environment.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(environment.getRequiredProperty("jdbc.password"));

        return dataSource;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();

        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        transactionManager.setDataSource(dataSource());

        return transactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan("sample.model");
        entityManagerFactoryBean.setJpaProperties(constructHibernateProperties());

        return entityManagerFactoryBean;
    }

    private Properties constructHibernateProperties() {
        final String[] names = {"hibernate.dialect", "hibernate.show_sql",
                "hibernate.hbm2ddl.auto", "hibernate.cache.provider_class", "hibernate.cache.use_second_level_cache",
                "hibernate.cache.region.factory_class"};

        final Properties result = new Properties();
        Arrays.stream(names).forEach(name -> {
            if (environment.containsProperty(name)) {
                result.put(name, environment.getProperty(name));
            }
        });

        return result;
    }
}

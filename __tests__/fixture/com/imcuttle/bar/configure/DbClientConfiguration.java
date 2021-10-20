package com.imcuttle.bar.configure;

import com.fenbi.common.db.DbClient;
import com.fenbi.common.db.configure.BaseDbClientConfigurationDynamic;
import com.fenbi.common.db.configure.BaseMySQLClientConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author linbonan
 */
@Configuration
@ConfigurationProperties(prefix = "tutorArmory.mysql")
public class DbClientConfiguration extends BaseDbClientConfigurationDynamic {

    @Primary
    @Bean(name = "dbClient")
    @Override
    public DbClient createDbClient() {
        return super.createDbClient();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("dbClient") DbClient dbClient) {
        return dbClient.getWriter().getDataSource();
    }

    @Bean(name = "txManager")
    @Primary
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource writeDataSource) {
        return new DataSourceTransactionManager(writeDataSource);
    }

}

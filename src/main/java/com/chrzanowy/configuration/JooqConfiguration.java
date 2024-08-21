package com.chrzanowy.configuration;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JooqConfiguration {

    @Bean
    public DSLContext dsl(DataSource dataSource) {
        return new DefaultDSLContext(configuration(dataSource));
    }

    public static DefaultConfiguration configuration(DataSource dataSource) {
        var defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.set(dataSource);
        defaultConfiguration.set(SQLDialect.H2);
        return defaultConfiguration;
    }
}
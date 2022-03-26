package com.awpghost.auth.configurations;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoAuditing;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import com.awpghost.auth.configurations.auditing.CustomAuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
@EnableArangoRepositories
@EnableArangoAuditing
public class ArangoDBConfigurations implements ArangoConfiguration {
    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder();
    }

    @Override
    public String database() {
        return "pocketchat";
    }

    @Bean
    AuditorAware<String> auditorAware() {
        return new CustomAuditorAwareImpl();
    }
}

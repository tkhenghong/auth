package com.awpghost.auth.configurations;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableArangoRepositories
public class ArangoDBConfigurations implements ArangoConfiguration {
    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder();
    }

    @Override
    public String database() {
        return "auth";
    }
}

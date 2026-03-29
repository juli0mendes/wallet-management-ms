package com.walletmanagement.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.walletmanagement.infrastructure.persistence.repository")
public class MongoPersistenceConfiguration {
}


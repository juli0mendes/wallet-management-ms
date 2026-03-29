package io.github.juli0mendes.walletmanagement.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "io.github.juli0mendes.walletmanagement.infrastructure.persistence.repository")
public class MongoPersistenceConfiguration {
}


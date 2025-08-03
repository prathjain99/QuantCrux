package com.quantcrux.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.quantcrux.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration class to ensure proper repository scanning
}
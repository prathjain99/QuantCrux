package com.quantcrux.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This configuration class enables Spring's @Scheduled annotation support
    // for the DataSourceHealthService scheduled tasks
}
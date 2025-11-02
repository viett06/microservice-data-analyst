package com.viet.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB configuration will be handled by Spring Boot auto-configuration
    // Custom configurations can be added here if needed
}

package com.devon.flashsale.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
	
	public static final String CURRENT_USER = "SYSTEM";
	
	/**
	 * @return the CURRENT_USER(i.e. SYSTEM)
	 */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(CURRENT_USER);
    }
}
package org.openmrs.module.disa.api.config;

import org.openmrs.module.disa.api.LabResultService;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class DisaModuleAPIConfig {

    @Bean
    public Gson gson() {
        // TODO add support for java.time.LocalDateTime objects
        return new GsonBuilder().create();
    }
}

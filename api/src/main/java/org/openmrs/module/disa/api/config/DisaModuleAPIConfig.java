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

    @Bean
    @Primary
    public ProxyFactoryBean proxyFactoryBean(LabResultService labResultService) {
        ProxyFactoryBean disaModuleProxyFactoryBean = new ProxyFactoryBean();
        disaModuleProxyFactoryBean.setTarget(labResultService);
        // Interceptor from OpenMRS core applicationContext-service.xml
        disaModuleProxyFactoryBean.setInterceptorNames("authorizationInterceptor");
        return disaModuleProxyFactoryBean;
    }
}

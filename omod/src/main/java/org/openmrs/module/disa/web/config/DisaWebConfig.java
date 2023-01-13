package org.openmrs.module.disa.web.config;

import org.openmrs.module.disa.web.delegate.ManageVLResultsDelegate;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class DisaWebConfig {

    @Bean
    @Primary
    public ProxyFactoryBean disaWebProxyFactoryBean(ManageVLResultsDelegate manageVLResultsDelegate) {
        ProxyFactoryBean disaWebProxyFactoryBean = new ProxyFactoryBean();
        disaWebProxyFactoryBean.setTarget(manageVLResultsDelegate);
        // Interceptor from OpenMRS core applicationContext-service.xml
        disaWebProxyFactoryBean.setInterceptorNames("authorizationInterceptor");
        return disaWebProxyFactoryBean;
    }

}

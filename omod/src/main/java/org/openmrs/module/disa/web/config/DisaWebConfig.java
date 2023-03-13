package org.openmrs.module.disa.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class DisaWebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // In previous versions of spring mvc controllers would favor accept headers over path extensions.
        // We need to support legacyui that makes requests with extensions that are not known to spring.
        // Disable favoring of path extension so we can load the visits tab (requests are made with .list extension).
        configurer.favorPathExtension(false);
    }
}

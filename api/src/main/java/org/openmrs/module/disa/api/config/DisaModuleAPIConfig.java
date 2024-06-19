package org.openmrs.module.disa.api.config;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.sync.CD4LabResultHandler;
import org.openmrs.module.disa.api.sync.CRAGLabResultHandler;
import org.openmrs.module.disa.api.sync.DuplicateRequestIdLookup;
import org.openmrs.module.disa.api.sync.FinalLabResultHandler;
import org.openmrs.module.disa.api.sync.HIVVLLabResultHandler;
import org.openmrs.module.disa.api.sync.LabResultHandler;
import org.openmrs.module.disa.api.sync.LabResultProcessor;
import org.openmrs.module.disa.api.sync.LocationLookup;
import org.openmrs.module.disa.api.sync.PatientNidLookup;
import org.openmrs.module.disa.api.sync.ProviderLookup;
import org.openmrs.module.disa.api.sync.TBLamLabResultHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class DisaModuleAPIConfig {

    @Bean
    public RestTemplate restTemplate(DisaUserAgentInterceptor disaUserAgentInterceptor) {
        List<HttpMessageConverter<?>> messageConverters = Collections
                .singletonList(new MappingJackson2HttpMessageConverter(objectMapper()));
        RestTemplate restTemplate = new RestTemplate(messageConverters);
        restTemplate.getInterceptors().add(disaUserAgentInterceptor);
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addDeserializer(LabResult.class, new LabResultDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    @Scope("prototype")
    public LabResultProcessor labResultHandler(
            DuplicateRequestIdLookup duplicateRequestIdLookup,
            PatientNidLookup patientNidLookup,
            ProviderLookup providerLookup,
            LocationLookup locationLookup,
            HIVVLLabResultHandler vlHandler,
            CD4LabResultHandler cd4Handler,
            TBLamLabResultHandler tbLamHandler,
            CRAGLabResultHandler cragHandler,
            FinalLabResultHandler finalLabResultHandler) {

        LabResultHandler[] chain = new LabResultHandler[] {
                duplicateRequestIdLookup,
                patientNidLookup,
                providerLookup,
                locationLookup,
                vlHandler,
                cd4Handler,
                tbLamHandler,
                cragHandler,
                finalLabResultHandler
        };

        LabResultHandler prev = chain[0];
        for (int i = 1; i < chain.length; i++) {
            prev.setNext(chain[i]);
            prev = chain[i];
        }

        return new LabResultProcessor(chain[0]);
    }
}

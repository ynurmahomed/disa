package org.openmrs.module.disa.api.config;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openmrs.module.disa.CD4LabResult;
import org.openmrs.module.disa.HIVVLLabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.TypeOfResult;
import org.openmrs.module.disa.api.sync.CD4LabResultHandler;
import org.openmrs.module.disa.api.sync.DuplicateRequestIdLookup;
import org.openmrs.module.disa.api.sync.FinalLabResultHandler;
import org.openmrs.module.disa.api.sync.HIVVLLabResultHandler;
import org.openmrs.module.disa.api.sync.LabResultHandler;
import org.openmrs.module.disa.api.sync.LabResultProcessor;
import org.openmrs.module.disa.api.sync.LocationLookup;
import org.openmrs.module.disa.api.sync.PatientNidLookup;
import org.openmrs.module.disa.api.sync.ProviderLookup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@Configuration
public class DisaModuleAPIConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LabResult.class, new LabResultDeserializer())
                .create();
    }

    @Bean
    public LabResultProcessor labResultHandler(
            DuplicateRequestIdLookup duplicateRequestIdLookup,
            PatientNidLookup patientNidLookup,
            ProviderLookup providerLookup,
            LocationLookup locationLookup,
            HIVVLLabResultHandler vlHandler,
            CD4LabResultHandler cd4Handler,
            FinalLabResultHandler finalLabResultHandler) {

        LabResultHandler[] chain = new LabResultHandler[] {
                duplicateRequestIdLookup,
                patientNidLookup,
                providerLookup,
                locationLookup,
                vlHandler,
                cd4Handler,
                finalLabResultHandler
        };

        LabResultHandler prev = chain[0];
        for (int i = 1; i < chain.length; i++) {
            prev.setNext(chain[i]);
            prev = chain[i];
        }

        return new LabResultProcessor(chain[0]);
    }

    private class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return new JsonPrimitive(formatter.format(src));
        }
    }

    private class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), formatter);
        }
    }

    private class LabResultDeserializer implements JsonDeserializer<LabResult> {
        @Override
        public LabResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            TypeOfResult typeOfResult = context.deserialize(jsonObject.get("typeOfResult"), TypeOfResult.class);
            if (typeOfResult == TypeOfResult.HIVVL) {
                return context.deserialize(json, HIVVLLabResult.class);
            } else if (typeOfResult == TypeOfResult.CD4) {
                return context.deserialize(json, CD4LabResult.class);
            }
            throw new JsonParseException("Unknown type of result: " + typeOfResult);
        }
    }
}

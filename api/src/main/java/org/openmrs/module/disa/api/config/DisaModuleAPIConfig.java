package org.openmrs.module.disa.api.config;

import java.lang.reflect.Type;

import org.openmrs.module.disa.CD4LabResult;
import org.openmrs.module.disa.HIVVLLabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.TypeOfResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@Configuration
public class DisaModuleAPIConfig {

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

    @Bean
    public Gson gson() {
        // TODO add support for java.time.LocalDateTime objects
        return new GsonBuilder()
                .registerTypeAdapter(LabResult.class, new LabResultDeserializer())
                .create();
    }
}

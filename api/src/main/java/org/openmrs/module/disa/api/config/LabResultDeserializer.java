package org.openmrs.module.disa.api.config;

import java.io.IOException;

import org.openmrs.module.disa.api.CD4LabResult;
import org.openmrs.module.disa.api.CRAGLabResult;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.TBLamLabResult;
import org.openmrs.module.disa.api.TypeOfResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class LabResultDeserializer extends JsonDeserializer<LabResult> {

    @Override
    public LabResult deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        TypeOfResult typeOfResult = p.getCodec().treeToValue(node.get("typeOfResult"), TypeOfResult.class);
        if (TypeOfResult.HIVVL == typeOfResult) {
            return p.getCodec().treeToValue(node, HIVVLLabResult.class);
        } else if (TypeOfResult.CD4 == typeOfResult) {
            return p.getCodec().treeToValue(node, CD4LabResult.class);
        } else if (TypeOfResult.TBLAM == typeOfResult) {
            return p.getCodec().treeToValue(node, TBLamLabResult.class);
        } else if (TypeOfResult.CRAG == typeOfResult) {
            return p.getCodec().treeToValue(node, CRAGLabResult.class);
        } else {
            throw new JsonParseException(p, "Unknown type of result: " + typeOfResult);
        }
    }

}

package org.openmrs.module.disa.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldDeserializeHivVlLabResults() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"id\":14227163,\"uuid\":\"disa\",\"createdBy\":\"disa\",\"createdAt\":\"2023-07-11T12:56:06\",\"updatedBy\":\"fgh\",\"updatedAt\":\"2023-07-18T15:03:17\",\"entityStatus\":\"ACTIVE\",\"finalResult\":\"59195\",\"nid\":\"0000000000/0000/00000\",\"requestId\":\"MZDISAPQM0000000\",\"referringRequestID\":null,\"firstName\":\"\",\"lastName\":\"\",\"gender\":\"F\",\"dateOfBirth\":null,\"location\":\"JULHO\",\"healthFacilityLabCode\":\"1040107\",\"requestingFacilityName\":\"CS 24 de Julho\",\"nameOfTechnicianRequestingTest\":null,\"encounter\":null,\"pregnant\":\"\",\"breastFeeding\":\"\",\"reasonForTest\":\"Routine\",\"harvestDate\":\"2023-03-10T00:00:00\",\"harvestType\":\"\",\"dateOfSampleReceive\":null,\"rejectedReason\":\"\",\"processingDate\":\"2023-07-07T14:10:00\",\"sampleType\":null,\"viralLoadResultQualitative\":\"Not Suppressed\",\"labResultDate\":\"2023-07-10T14:33:00\",\"aprovedBy\":null,\"labComments\":null,\"labResultStatus\":\"NOT_PROCESSED\",\"notProcessingCause\":\"NID_NOT_FOUND\",\"artRegimen\":\"\",\"primeiraLinha\":\"\",\"segundaLinha\":\"\",\"dataDeInicioDoTARV\":null,\"requestingProvinceName\":\"Zambezia\",\"requestingDistrictName\":\"Quelimane\",\"synchronizedBy\":\"c9c8c8bb-67b3-41f7-948a-c58ae02dca46\",\"ageInYears\":34,\"typeOfResult\":\"HIVVL\",\"registeredDateTime\":null,\"lastViralLoadResult\":\"\",\"lastViralLoadDate\":\"\",\"hivViralLoadResult\":null,\"viralLoadResultCopies\":\"59195\",\"viralLoadResultLog\":\"4.77\"}";
        HIVVLLabResult hivVl = objectMapper.readValue(json, HIVVLLabResult.class);
        assertThat(hivVl.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(hivVl, notNullValue());
    }

    @Test
    public void shouldDeserializePage() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"content\":[{\"id\":14227163,\"uuid\":\"disa\",\"createdBy\":\"disa\",\"createdAt\":\"2023-07-11T12:56:06\",\"updatedBy\":\"fgh\",\"updatedAt\":\"2023-07-18T15:03:17\",\"entityStatus\":\"ACTIVE\",\"finalResult\":\"          59195\",\"nid\":\"0000000000/0000/00000\",\"requestId\":\"MZDISAPQM0000000\",\"referringRequestID\":null,\"firstName\":\"\",\"lastName\":\"\",\"gender\":\"F\",\"dateOfBirth\":null,\"location\":\"JULHO\",\"healthFacilityLabCode\":\"1040107\",\"requestingFacilityName\":\"CS 24 de Julho\",\"nameOfTechnicianRequestingTest\":null,\"encounter\":null,\"pregnant\":\"\",\"breastFeeding\":\"\",\"reasonForTest\":\"Routine\",\"harvestDate\":\"2023-03-10T00:00:00\",\"harvestType\":\"\",\"dateOfSampleReceive\":null,\"rejectedReason\":\"\",\"processingDate\":\"2023-07-07T14:10:00\",\"sampleType\":null,\"viralLoadResultQualitative\":\"Not Suppressed\",\"labResultDate\":\"2023-07-10T14:33:00\",\"aprovedBy\":null,\"labComments\":null,\"labResultStatus\":\"NOT_PROCESSED\",\"notProcessingCause\":\"NID_NOT_FOUND\",\"artRegimen\":\"\",\"primeiraLinha\":\"\",\"segundaLinha\":\"\",\"dataDeInicioDoTARV\":null,\"requestingProvinceName\":\"Zambezia\",\"requestingDistrictName\":\"Quelimane\",\"synchronizedBy\":\"c9c8c8bb-67b3-41f7-948a-c58ae02dca46\",\"ageInYears\":34,\"typeOfResult\":\"HIVVL\",\"registeredDateTime\":null,\"lastViralLoadResult\":\"\",\"lastViralLoadDate\":\"\",\"hivViralLoadResult\":null,\"viralLoadResultCopies\":\"          59195\",\"viralLoadResultLog\":\" 4.77\"},{\"id\":7815551,\"uuid\":\"disa\",\"createdBy\":\"disa\",\"createdAt\":\"2023-06-29T12:53:34\",\"updatedBy\":\"fgh\",\"updatedAt\":\"2023-06-29T11:16:31\",\"entityStatus\":\"ACTIVE\",\"finalResult\":\"INDETECTAVEL\",\"nid\":\"0000000000/0000/00000\",\"requestId\":\"MZDISAPQM0000000\",\"referringRequestID\":null,\"firstName\":\"\",\"lastName\":\"\",\"gender\":\"M\",\"dateOfBirth\":null,\"location\":\"DEZET\",\"healthFacilityLabCode\":\"1040106\",\"requestingFacilityName\":\"CS 17 De Setembro\",\"nameOfTechnicianRequestingTest\":null,\"encounter\":null,\"pregnant\":\"\",\"breastFeeding\":\"\",\"reasonForTest\":\"Routine\",\"harvestDate\":\"2023-06-27T14:18:00\",\"harvestType\":\"\",\"dateOfSampleReceive\":null,\"rejectedReason\":\"\",\"processingDate\":\"2023-06-29T08:59:00\",\"sampleType\":null,\"viralLoadResultQualitative\":\"Suppressed\",\"labResultDate\":\"2023-06-29T09:07:00\",\"aprovedBy\":null,\"labComments\":null,\"labResultStatus\":\"NOT_PROCESSED\",\"notProcessingCause\":\"NID_NOT_FOUND\",\"artRegimen\":\"\",\"primeiraLinha\":\"\",\"segundaLinha\":\"\",\"dataDeInicioDoTARV\":null,\"requestingProvinceName\":\"Zambezia\",\"requestingDistrictName\":\"Quelimane\",\"synchronizedBy\":null,\"ageInYears\":21,\"typeOfResult\":\"HIVVL\",\"registeredDateTime\":null,\"lastViralLoadResult\":\"\",\"lastViralLoadDate\":\"\",\"hivViralLoadResult\":\"INDETECTAVEL\",\"viralLoadResultCopies\":null,\"viralLoadResultLog\":null}],\"pageable\":{\"sort\":{\"unsorted\":false,\"sorted\":true},\"pageSize\":10,\"pageNumber\":0,\"offset\":0,\"paged\":true,\"unpaged\":false},\"last\":true,\"totalPages\":1,\"totalElements\":2,\"sort\":{\"unsorted\":false,\"sorted\":true},\"numberOfElements\":2,\"first\":true,\"size\":10,\"number\":0}";
        TypeReference<Page<LabResult>> typeReference = new TypeReference<Page<LabResult>>() {
        };
        Page<LabResult> page = objectMapper.readValue(json, typeReference);
        assertThat(page, notNullValue());
    }
}

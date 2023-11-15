package org.openmrs.module.disa.web.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.CD4LabResult;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.test.BaseContextMockTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MapUnprocessedLabResultsControllerTest extends BaseContextMockTest {

    @Mock
    private LabResultService labResultService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private DisaService disaService;

    @InjectMocks
    private MapUnprocessedLabResultsController mapUnprocessedLabResultsController;

    private MockMvc mockMvc;

    long labResultId = 53038017l;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mapUnprocessedLabResultsController).build();
    }

    @Test
    public void mapIndetifiersShouldFailWhenNoPatientSpecifed() throws Exception {

        CD4LabResult cd4 = new CD4LabResult(labResultId);

        when(labResultService.getById(labResultId)).thenReturn(cd4);

        when(messageSourceService.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("Unexpected Error!");

        mockMvc.perform(post("/module/disa/managelabresults/" + labResultId + "/map"))
                .andExpect(model().attribute("flashMessage", "Unexpected Error!"))
                .andExpect(status().isOk())
                .andExpect(view().name("/module/disa/managelabresults/map"));
    }

    @Test
    public void mapIndetifiersShouldBackToSearchInterfaceWithParams() throws Exception {

        CD4LabResult cd4 = new CD4LabResult(labResultId);

        when(labResultService.getById(labResultId)).thenReturn(cd4);

        Patient patient = new Patient();
        patient.addIdentifier(new PatientIdentifier());
        when(disaService.mapIdentifier(anyString(), any(LabResult.class))).thenReturn(patient);

        when(messageSourceService.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("Map Successful!");

        mockMvc.perform(post("/module/disa/managelabresults/" + labResultId + "/map")
                .param("patientUuid", "f6af3bb7-7690-4639-b922-7a631fe5a984"))
                .andExpect(model().attribute("flashMessage", "Map Successful!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**/managelabresults*"));
    }

}

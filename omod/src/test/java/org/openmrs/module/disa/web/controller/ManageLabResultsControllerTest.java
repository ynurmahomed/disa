package org.openmrs.module.disa.web.controller;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.CD4LabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.TypeOfResult;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.sync.SyncStatusService;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ManageLabResultsControllerTest extends BaseContextMockTest {

    private static final String SISMA_CODE = "1040107";

    @Mock
    private LabResultService labResultService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private AdministrationService administrationService;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private OrgUnitService orgUnitService;

    @Mock
    private SyncStatusService syncStatusService;

    @InjectMocks
    private ManageLabResultsController manageLabResultsController;

    private MockMvc mockMvc;

    private GlobalProperty sismaCodesGP = new GlobalProperty(Constants.DISA_SISMA_CODE, SISMA_CODE);

    private OrgUnit cs24deJulho = new OrgUnit(SISMA_CODE, "Zambezia", "Quelimane", "24 de Julho CSURB");

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(manageLabResultsController).build();
    }

    @Test
    public void searchShouldSetPageTitle() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

        when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        mockMvc.perform(get("/module/disa/managelabresults/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(view().name("/module/disa/managelabresults/index"));
    }

    @Test
    public void searchShouldPopulateSismaCodes() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        mockMvc.perform(get("/module/disa/managelabresults/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgUnits", hasValue("24 de Julho CSURB - " + SISMA_CODE)))
                .andExpect(view().name("/module/disa/managelabresults/index"));
    }

    @Test
    public void searchShouldNotPopulateSismaCodesWithUnknownOrgUnit() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(null);

        mockMvc.perform(get("/module/disa/managelabresults/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orgUnits", is(anEmptyMap())))
                .andExpect(view().name("/module/disa/managelabresults/index"));
    }

    @Test
    public void searchShouldSetTheExportUri() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        mockMvc.perform(get("/module/disa/managelabresults/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("exportUri"))
                .andExpect(view().name("/module/disa/managelabresults/index"));
    }

    @Test
    public void searchShouldSetSearchResults() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        Page<LabResult> labResults = new Page<>(0, 5, 0, Collections.emptyList());

        when(labResultService.search(
            any(LocalDate.class),
            any(LocalDate.class),
            any(String.class),
            any(LabResultStatus.class),
            any(NotProcessingCause.class),
            any(TypeOfResult.class),
            any(String.class),
            anyListOf(String.class),
            any(String.class),
            anyInt(),
            anyInt(),
            any(String.class),
            any(String.class)))
        .thenReturn(labResults);

        mockMvc.perform(get("/module/disa/managelabresults/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("disaPage", instanceOf(Page.class)))
                .andExpect(view().name("/module/disa/managelabresults/index"));
    }

    @Test
    public void searchShouldSaveLastSearchParamsInSession() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        HttpSession session = mockMvc.perform(get("/module/disa/managelabresults").param("vlState", "NOT_PROCESSED"))
            .andExpect(status().isOk())
            .andExpect(view().name("/module/disa/managelabresults/index"))
            .andReturn().getRequest().getSession();

        assertThat((MultiValueMap<String, String>) session.getAttribute("lastSearchParams"), 
            hasEntry(equalTo("vlState"), hasItem("NOT_PROCESSED")));

    }

    @Test
    public void searchShouldReturnAnErrorPageWithFlashMessage() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

            when(orgUnitService.getOrgUnitByCode(SISMA_CODE))
            .thenReturn(cs24deJulho);

        when(labResultService.search(
            any(LocalDate.class),
            any(LocalDate.class),
            any(String.class),
            any(LabResultStatus.class),
            any(NotProcessingCause.class),
            any(TypeOfResult.class),
            any(String.class),
            anyListOf(String.class),
            any(String.class),
            anyInt(),
            anyInt(),
            any(String.class),
            any(String.class)))
        .thenThrow(new DisaModuleAPIException("Unexpected Error!"));

        mockMvc.perform(get("/module/disa/managelabresults").param("vlState", "NOT_PROCESSED"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("flashMessage", "Unexpected Error!"))
            .andExpect(view().name("/module/disa/managelabresults/error"))
            .andReturn();
    }

    @Test
    public void searchJsonShouldReturnJsonSearchResults() throws Exception {

        when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
            .thenReturn(sismaCodesGP);

        Page<LabResult> labResults = new Page<>(0, 5, 0, Arrays.asList(new CD4LabResult(1234l)));

        when(labResultService.search(
            any(LocalDate.class),
            any(LocalDate.class),
            any(String.class),
            any(LabResultStatus.class),
            any(NotProcessingCause.class),
            any(TypeOfResult.class),
            any(String.class),
            anyListOf(String.class),
            any(String.class),
            anyInt(),
            anyInt(),
            any(String.class),
            any(String.class)))
        .thenReturn(labResults);

        when(objectMapper.writeValueAsString(any(Page.class)))
            .thenCallRealMethod();

        mockMvc.perform(get("/module/disa/managelabresults/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resultList[0].id", equalTo(1234)))
                .andExpect(jsonPath("$.resultList[0].typeOfResult", equalTo(TypeOfResult.CD4.toString())));
    }

    @Test
    public void exportShouldValidateDates() throws Exception {
        LinkedMultiValueMap<String, String> searchParams = new LinkedMultiValueMap<>();
        searchParams.add("vlState", LabResultStatus.NOT_PROCESSED.toString());

        when(messageSourceService.getMessage(any(String.class), any(), any(Locale.class)))
                .thenReturn("Missing start date or end date.");

        mockMvc.perform(get("/module/disa/managelabresults/export")
                .sessionAttr("lastSearchParams", searchParams))
                .andExpect(model().attribute("flashMessage", equalTo("Missing start date or end date.")));
    }

    @Test
    public void exportShouldRedirectBackToSearchInterfaceWithParams() throws Exception {
        LinkedMultiValueMap<String, String> searchParams = new LinkedMultiValueMap<>();
        searchParams.add("vlState", LabResultStatus.NOT_PROCESSED.toString());
        searchParams.add("vlSisma", "1040107");

        when(messageSourceService.getMessage(any(String.class), any(), any(Locale.class)))
                .thenReturn("Missing start date or end date.");

        mockMvc.perform(get("/module/disa/managelabresults/export")
                .sessionAttr("lastSearchParams", searchParams))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**/managelabresults*vlState=NOT_PROCESSED&vlSisma=1040107*"));
    }

    @Test
    public void exportShouldRedirectToDownload() throws Exception {
        MultiValueMap<String, String> searchParams = new LinkedMultiValueMap<>();
        searchParams.add("vlState", LabResultStatus.NOT_PROCESSED.toString());
        searchParams.add("vlSisma", "1040107");

        mockMvc.perform(get("/module/disa/managelabresults/export")
                .sessionAttr("lastSearchParams", searchParams)
                .param("startDate", "2023/08/01")
                .param("endDate", "2023/08/07"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/**/download*"));
    }

    @Test
    public void downloadShouldGenerateReport() throws Exception {

        CD4LabResult labResult = new CD4LabResult(1234l);
        labResult.setCreatedAt(LocalDateTime.now());
        List<LabResult> labResults = Arrays.asList(labResult);

        when(labResultService.getAll(
                any(LocalDate.class),
                any(LocalDate.class),
                any(String.class),
                any(LabResultStatus.class),
                any(NotProcessingCause.class),
                any(String.class),
                anyListOf(String.class)))
                .thenReturn(labResults);

        mockMvc.perform(get("/module/disa/managelabresults/download")
                .param("startDate", "2023/08/01")
                .param("endDate", "2023/08/07"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/ms-excel"));
    }

}

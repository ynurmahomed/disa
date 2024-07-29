package org.openmrs.module.disa.api;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.impl.LabResultServiceImpl;
import org.openmrs.test.BaseContextMockTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public class LabResultServiceImplTest extends BaseContextMockTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Mock
    private DisaAPIHttpClient client;

    @Mock
    private OrgUnitService orgUnitService;

    @InjectMocks
    private LabResultServiceImpl labResultService;

    @Test
    public void getByRequestIdShouldReturnTheLabResult() throws IOException, URISyntaxException {
        LabResult expected = new HIVVLLabResult();
        when(client.getResultById(1l)).thenReturn(expected);
        LabResult result = labResultService.getById(1l);
        Assert.assertEquals(expected, result);
    }

    @Test(expected = DisaModuleAPIException.class)
    public void getByRequestIdShouldThrowException() throws IOException, URISyntaxException {
        when(client.getResultById(1l))
                .thenThrow(new URISyntaxException("", ""));
        labResultService.getById(1l);
    }

    @Test
    public void deleteByRequestIdShouldDeleteTheLabResult() throws IOException, URISyntaxException {
        labResultService.deleteById(1l);
        verify(client, Mockito.times(1)).deleteResultById(1l);
    }

    @Test(expected = DisaModuleAPIException.class)
    public void deleteByRequestIdSholdThrowException() throws IOException, URISyntaxException {
        doThrow(new URISyntaxException("", ""))
                .when(client).deleteResultById(1l);
        labResultService.deleteById(1l);
    }

    @Test
    public void testReallocateLabResult() throws IOException, URISyntaxException {
        LabResult labResult = new HIVVLLabResult(1l);
        when(client.getResultById(anyLong())).thenReturn(labResult);
        OrgUnit destination = new OrgUnit();
        destination.setCode("code");
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setCode("code");
        orgUnit.setFacility("facility");
        orgUnit.setDistrict("district");
        orgUnit.setProvince("province");
        when(orgUnitService.getOrgUnitByCode("code")).thenReturn(orgUnit);
        LabResult result = labResultService.reallocateLabResult(1l, destination);
        assertThat(result.getHealthFacilityLabCode(), is(orgUnit.getCode()));
        assertThat(result.getRequestingFacilityName(), is(orgUnit.getFacility()));
        assertThat(result.getRequestingDistrictName(), is(orgUnit.getDistrict()));
        assertThat(result.getRequestingProvinceName(), is(orgUnit.getProvince()));
        assertThat(result.getLabResultStatus(), is(LabResultStatus.PENDING));
        verify(orgUnitService, Mockito.times(1)).getOrgUnitByCode("code");
        verify(client, Mockito.times(1)).updateResult(result);
    }

    @Test
    public void testRescheduleLabResult() throws IOException, URISyntaxException {
        LabResult labResult = new HIVVLLabResult(1l);
        when(client.getResultById(anyLong())).thenReturn(labResult);
        labResultService.rescheduleLabResult(1l);
        verify(client, Mockito.times(1)).updateResult(any(LabResult.class));
    }

    @Test
    public void searchShouldFailIfUserIsNotAuthorized() throws IOException, URISyntaxException {
        when(client.searchLabResults(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                any(LabResultStatus.class),
                any(NotProcessingCause.class),
                any(TypeOfResult.class),
                anyString(),
                anyListOf(String.class),
                anyString(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden"));

        String sismaCode = "1100811";

        exceptionRule.expect(DisaModuleAPIException.class);
        // exceptionRule.expectMessage("The user does not have permission");

        labResultService.search(
                LocalDate.now(),
                LocalDate.now(),
                "",
                null,
                null,
                null,
                "",
                Collections.singletonList(sismaCode),
                "",
                0,
                0,
                "",
                "");
    }
}

package org.openmrs.module.disa.api;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.impl.LabResultServiceImpl;
import org.openmrs.test.BaseContextMockTest;

public class LabResultServiceImplTest extends BaseContextMockTest {

    @Mock
    private DisaAPIHttpClient client;

    @Mock
    private OrgUnitService orgUnitService;

    @InjectMocks
    private LabResultServiceImpl labResultService;

    @Test
    public void getByRequestIdShouldReturnTheLabResult() throws IOException, URISyntaxException {
        Disa expected = new Disa();
        when(client.getResultByRequestId("requestId")).thenReturn(expected);
        Disa result = labResultService.getByRequestId("requestId");
        Assert.assertEquals(expected, result);
    }

    @Test(expected = DisaModuleAPIException.class)
    public void getByRequestIdShouldThrowException() throws IOException, URISyntaxException {
        when(client.getResultByRequestId("requestId"))
                .thenThrow(new URISyntaxException("", ""));
        labResultService.getByRequestId("requestId");
    }

    @Test
    public void deleteByRequestIdShouldDeleteTheLabResult() throws IOException, URISyntaxException {
        labResultService.deleteByRequestId("requestId");
        verify(client, Mockito.times(1)).deleteResultByRequestId("requestId");
    }

    @Test(expected = DisaModuleAPIException.class)
    public void deleteByRequestIdSholdThrowException() throws IOException, URISyntaxException {
        doThrow(new URISyntaxException("", ""))
                .when(client).deleteResultByRequestId("requestId");
        labResultService.deleteByRequestId("requestId");
    }

    @Test
    public void testReallocateLabResult() throws IOException, URISyntaxException {
        Disa labResult = new Disa();
        OrgUnit destination = new OrgUnit();
        destination.setCode("code");
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setCode("code");
        orgUnit.setFacility("facility");
        orgUnit.setDistrict("district");
        orgUnit.setProvince("province");
        when(orgUnitService.getOrgUnitByCode("code")).thenReturn(orgUnit);
        Disa result = labResultService.reallocateLabResult(labResult, destination);
        assertThat(result.getHealthFacilityLabCode(), is(orgUnit.getCode()));
        assertThat(result.getRequestingFacilityName(), is(orgUnit.getFacility()));
        assertThat(result.getRequestingDistrictName(), is(orgUnit.getDistrict()));
        assertThat(result.getRequestingProvinceName(), is(orgUnit.getProvince()));
        assertThat(result.getViralLoadStatus(), is("PENDING"));
        verify(orgUnitService, Mockito.times(1)).getOrgUnitByCode("code");
        verify(client, Mockito.times(1)).updateResult(result);
    }

    @Test
    public void testRescheduleLabResult() throws IOException, URISyntaxException {
        Disa labResult = new Disa();
        labResultService.rescheduleLabResult(labResult);
        assertThat(labResult.getViralLoadStatus(), is("PENDING"));
        verify(client, Mockito.times(1)).updateResult(labResult);
    }

}

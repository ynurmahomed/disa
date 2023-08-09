package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.SyncLog;
import org.openmrs.module.disa.api.db.DisaDAO;
import org.openmrs.module.disa.api.impl.DisaServiceImpl;
import org.openmrs.test.BaseContextMockTest;

public class DisaServiceUnitTest extends BaseContextMockTest {

    @Mock
    private LocationService locationService;

    @Mock
    private EncounterService encounterService;

    @Mock
    private LabResultService labResultService;

    @Mock
    private DisaDAO disaDAO;

    @InjectMocks
    private DisaServiceImpl disaService;

    @Test
    public void shouldSaveTheEncounterForProcessedLabResults() {
        Encounter encounter = new Encounter();
        Patient patient = new Patient(1);
        encounter.setPatient(patient);

        LabResult labResult = new HIVVLLabResult();
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PROCESSED);

        Location location = new Location();
        location.setUuid("93377be8-1093-47f0-ad05-e014d3a14615");
        when(locationService.getDefaultLocation()).thenReturn(location);

        disaService.handleProcessedLabResult(labResult, encounter);

        verify(encounterService, times(1)).saveEncounter(encounter);
    }

    @Test
    public void shouldCreateFsrLogForProcessedLabResults() {
        Encounter encounter = new Encounter();
        Patient patient = new Patient(1);
        encounter.setPatient(patient);

        LabResult labResult = new HIVVLLabResult();
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PROCESSED);

        Location location = new Location();
        location.setUuid("93377be8-1093-47f0-ad05-e014d3a14615");
        when(locationService.getDefaultLocation()).thenReturn(location);

        disaService.handleProcessedLabResult(labResult, encounter);

        verify(disaDAO, times(1)).saveSyncLog(any(SyncLog.class));
    }

    @Test
    public void shouldUpdateProcessedLabResults() {

        Encounter encounter = new Encounter();
        Patient patient = new Patient(1);
        encounter.setPatient(patient);

        LabResult labResult = new HIVVLLabResult();
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PROCESSED);

        Location defaultLocation = new Location();
        defaultLocation.setUuid("93377be8-1093-47f0-ad05-e014d3a14615");
        when(locationService.getDefaultLocation()).thenReturn(defaultLocation);

        disaService.handleProcessedLabResult(labResult, encounter);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
        assertThat(labResult.getSynchronizedBy(), is(defaultLocation.getUuid()));
        verify(labResultService, times(1)).updateLabResult(any(LabResult.class));
    }

}

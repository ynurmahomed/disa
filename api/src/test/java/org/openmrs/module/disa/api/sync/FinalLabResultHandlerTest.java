package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.test.BaseContextMockTest;

public class FinalLabResultHandlerTest extends BaseContextMockTest {

    @Mock
    private LabResultService labResultService;

    @Mock
    private LocationService locationService;

    @Mock
    private LabResultHandler next;

    @Mock
    private EncounterService encounterService;

    @Mock
    private DisaService disaService;

    @InjectMocks
    private FinalLabResultHandler finalLabResultHandler;

    @Before
    public void before() {
        finalLabResultHandler.setNext(next);
    }

    @Test
    public void shouldUpdateNotProcessedLabResult() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
        labResult.setNotProcessingCause(NotProcessingCause.DUPLICATE_NID);

        finalLabResultHandler.getSyncContext().put(BaseLabResultHandler.ENCOUNTER_KEY, new Encounter());

        finalLabResultHandler.handle(labResult);

        verify(labResultService, times(1)).updateLabResult(labResult);
    }

    @Test
    public void shouldTerminateTheChainIfStatusIsNotProcessed() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
        labResult.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);

        finalLabResultHandler.getSyncContext().put(BaseLabResultHandler.ENCOUNTER_KEY, new Encounter());

        finalLabResultHandler.handle(labResult);

        verify(next, never()).handle(labResult);
    }

    @Test
    public void shouldClearTheSyncContext() {

        Encounter encounter = new Encounter();
        Patient patient = new Patient(1);
        encounter.setPatient(patient);
        finalLabResultHandler.getSyncContext().put(BaseLabResultHandler.ENCOUNTER_KEY, encounter);

        LabResult labResult = new HIVVLLabResult();
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PROCESSED);

        Location defaultLocation = new Location();
        defaultLocation.setUuid("93377be8-1093-47f0-ad05-e014d3a14615");
        when(locationService.getDefaultLocation()).thenReturn(defaultLocation);

        finalLabResultHandler.handle(labResult);

        assertThat(finalLabResultHandler.getSyncContext(), is(anEmptyMap()));
    }

}

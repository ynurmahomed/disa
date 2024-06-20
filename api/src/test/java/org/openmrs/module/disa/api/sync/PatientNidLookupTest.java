package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.util.Notifier;
import org.openmrs.test.BaseContextMockTest;

public class PatientNidLookupTest extends BaseContextMockTest {
    @Mock
    private DisaService disaService;

    @Mock
    private LabResultHandler next;

    @Mock
    private Notifier notifier;

    @InjectMocks
    private PatientNidLookup patientNidHandler;

    @Before
    public void before() {
        patientNidHandler.setNext(next);
    }

    @Test
    public void shouhouldSetLabResultAsNotProcessedNidNotFound() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        when(disaService.getPatientByNid(anyString()))
                .thenReturn(Collections.emptyList());

        patientNidHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.NID_NOT_FOUND));

        // Calls the next handler
        verify(next, times(1)).handle(labResult);
    }

    @Test
    public void shouhouldSetLabResultAsNotProcessedDuplicateNid() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        when(disaService.getPatientByNid(anyString()))
                .thenReturn(Arrays.asList(1, 2));

        patientNidHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.DUPLICATE_NID));

        // Calls the next handler
        verify(next, times(1)).handle(labResult);
    }

    @Test
    public void shouhouldSendANotificationForNotProcessedDuplicateNid() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        when(disaService.getPatientByNid(anyString()))
                .thenReturn(Arrays.asList(1, 2));

        doNothing().when(notifier).notify(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        patientNidHandler.handle(labResult);

        // Sends a notification
        verify(notifier, times(1)).notify(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString());
    }

    @Test
    public void shouhouldAddThePatientToSyncContext() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setNid("000000000/0000/00000");
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        Patient patient = new Patient();

        when(disaService.getPatientByNid(anyString()))
                .thenReturn(Arrays.asList(1));

        when(disaService.getPatientByPatientId(anyInt()))
                .thenReturn(Arrays.asList(patient));

        patientNidHandler.handle(labResult);

        assertThat(patientNidHandler.getSyncContext().get(PatientNidLookup.PATIENT_KEY), is(patient));

        // Calls the next handler
        verify(next, times(1)).handle(labResult);
    }
}

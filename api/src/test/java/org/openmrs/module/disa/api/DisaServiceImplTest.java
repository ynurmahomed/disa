package org.openmrs.module.disa.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.db.DisaDAO;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.impl.DisaServiceImpl;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;

public class DisaServiceImplTest extends BaseContextMockTest {

    @Mock
    private LabResultService labResultService;

    @Mock
    private PatientService patientService;

    @Mock
    private LocationService locationService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private DisaDAO dao;

    @InjectMocks
    private DisaServiceImpl disaServiceImpl;

    @Test
    public void mapIdentifierShouldSavePatientIdentifier() {
        String patientUuid = "patientUuid";
        String nid = "12345";
        String requestId = "requestId";
        LabResult result1 = new HIVVLLabResult(1l);
        String healthFacilityLabCode = "1040107";
        result1.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
        result1.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
        result1.setNid(nid);
        result1.setRequestId(requestId);
        result1.setHealthFacilityLabCode(healthFacilityLabCode);
        LabResult result2 = new CD4LabResult(2l);
        result2.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
        result2.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
        result2.setNid(nid);
        result2.setRequestId(requestId);
        result1.setHealthFacilityLabCode(healthFacilityLabCode);
        Patient patient = new Patient();
        patient.addIdentifier(new PatientIdentifier());

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(result1.getNid()), anyListOf(PatientIdentifierType.class), any(),
                any(), any()))
                .thenReturn(new ArrayList<>());
        when(labResultService.getAll(any(), any(), any(), eq(LabResultStatus.NOT_PROCESSED), any(), eq(nid),
                anyList())).thenReturn(Arrays.asList(result1, result2));

        disaServiceImpl.mapIdentifier(patientUuid, result1);

        verify(patientService, times(1)).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, times(1)).rescheduleLabResult(result1.getId());
        verify(labResultService, times(1)).rescheduleLabResult(result2.getId());
    }

    @Test(expected = DisaModuleAPIException.class)
    public void mapIdentifierShouldThrowExceptionWhenResultIsProcessed() {
        String patientUuid = "patientUuid";
        LabResult disa = new HIVVLLabResult();
        disa.setLabResultStatus(LabResultStatus.PROCESSED);
        disa.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyListOf(PatientIdentifierType.class), any(),
                any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, times(0)).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, times(0)).rescheduleLabResult(disa.getId());
    }

    @Test(expected = DisaModuleAPIException.class)
    public void mapIdentifierShouldThrowExceptionWhenResultIsPending() {
        String patientUuid = "patientUuid";
        LabResult disa = new HIVVLLabResult();
        disa.setLabResultStatus(LabResultStatus.PENDING);
        disa.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyListOf(PatientIdentifierType.class), any(),
                any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, never()).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, never()).rescheduleLabResult(disa.getId());
    }

}

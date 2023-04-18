package org.openmrs.module.disa.api;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
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
import org.openmrs.module.disa.Disa;
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
    private DisaDAO dao;

    @InjectMocks
    private DisaServiceImpl disaServiceImpl;

    @Test
    public void mapIdentifierShouldSavePatientIdentifier() {
        String patientUuid = "patientUuid";
        Disa disa = new Disa();
        disa.setViralLoadStatus("NOT_PROCESSED");
        disa.setNotProcessingCause("NID_NOT_FOUND");
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyList(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, times(1)).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, times(1)).rescheduleLabResult(any(Disa.class));
    }

    @Test(expected = DisaModuleAPIException.class)
    public void mapIdentifierShouldThrowExceptionWhenResultIsProcessed() {
        String patientUuid = "patientUuid";
        Disa disa = new Disa();
        disa.setViralLoadStatus("PROCESSED");
        disa.setNotProcessingCause("NID_NOT_FOUND");
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyList(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, times(0)).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, times(0)).rescheduleLabResult(any(Disa.class));
    }

    @Test
    public void mapIdentifierShouldNotSavePatientIdentifierWhenItAlreadyExists() {
        String patientUuid = "patientUuid";
        Disa disa = new Disa();
        disa.setViralLoadStatus("NOT_PROCESSED");
        disa.setNotProcessingCause("NID_NOT_FOUND");
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);

        PatientIdentifier disaNidIdentifier = new PatientIdentifier("12345", identifierType, null);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyList(), any(), any(), any()))
                .thenReturn(Arrays.asList(disaNidIdentifier));

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, never()).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, never()).rescheduleLabResult(any(Disa.class));
    }

}

package org.openmrs.module.disa.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyListOf(PatientIdentifierType.class), any(), any(), any()))
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
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyListOf(PatientIdentifierType.class), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, times(0)).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, times(0)).rescheduleLabResult(any(Disa.class));
    }

    @Test(expected = DisaModuleAPIException.class)
    public void mapIdentifierShouldThrowExceptionWhenResultIsPending() {
        String patientUuid = "patientUuid";
        Disa disa = new Disa();
        disa.setViralLoadStatus("PENDING");
        disa.setNotProcessingCause("NID_NOT_FOUND");
        disa.setNid("12345");
        disa.setRequestId("requestId");
        Patient patient = new Patient();

        when(patientService.getPatientByUuid(patientUuid)).thenReturn(patient);
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid(Constants.DISA_NID);
        when(patientService.getPatientIdentifierTypeByUuid(Constants.DISA_NID)).thenReturn(identifierType);
        when(patientService.getPatientIdentifiers(eq(disa.getNid()), anyListOf(PatientIdentifierType.class), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        disaServiceImpl.mapIdentifier(patientUuid, disa);

        verify(patientService, never()).savePatientIdentifier(any(PatientIdentifier.class));
        verify(labResultService, never()).rescheduleLabResult(any(Disa.class));
    }

    @Test
    public void getPatientsToMapSuggestionShouldReturnPatientsWithIdentifiers() {
        Disa disa = new Disa();
        disa.setFirstName("John");
        disa.setLastName("Doe");
        Patient patient = new Patient();

        when(patientService.getPatients(
                any(String.class),
                any(String.class),
                anyListOf(PatientIdentifierType.class),
                any(Boolean.class)))
                .thenReturn(Arrays.asList(patient));

        List<Patient> suggestion = disaServiceImpl.getPatientsToMapSuggestion(disa);

        assertThat(suggestion, hasSize(0));
    }

}

package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.api.CD4LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.test.BaseContextMockTest;

public class CD4LabResultHandlerTest extends BaseContextMockTest {

    @Mock
    private EncounterService encounterService;

    @Mock
    private FormService formService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private PersonService personService;

    @InjectMocks
    private CD4LabResultHandler cd4LabResultHandler;

    private CD4LabResult labResult;

    @Before
    public void setUp() {
        labResult = new CD4LabResult();
        labResult.setLabResultStatus(LabResultStatus.PENDING);
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setFinalResult("385");
        labResult.setHealthFacilityLabCode("1040107");
        labResult.setPregnant("");
        labResult.setBreastFeeding("");
        labResult.setReasonForTest("");
        labResult.setPrimeiraLinha("");
        labResult.setSegundaLinha("");
        labResult.setLabResultDate(LocalDateTime.now());

        Provider provider = new Provider();
        cd4LabResultHandler.getSyncContext().put(ProviderLookup.PROVIDER_KEY, provider);

        Patient patient = new Patient();
        cd4LabResultHandler.getSyncContext().put(PatientNidLookup.PATIENT_KEY, patient);

        Location location = new Location();
        location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
        cd4LabResultHandler.getSyncContext().put(LocationLookup.LOCATION_KEY, location);
    }

    @Test
    public void shouldNotProcessNegativeResults() {
        labResult.setFinalResult("-1");
        cd4LabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldNotProcessDecimalResults() {
        labResult.setFinalResult("22.5");
        cd4LabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldProcessZero() {
        labResult.setFinalResult("0");
        cd4LabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

    @Test
    public void shouldProcessPositiveInteger() {
        labResult.setFinalResult("123");
        cd4LabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

    @Test
    public void shouldProcessPositiveIntegersWithSpaces() {
        labResult.setFinalResult(" 123  ");
        cd4LabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

}

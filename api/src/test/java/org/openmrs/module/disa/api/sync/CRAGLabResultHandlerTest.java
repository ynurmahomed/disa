package org.openmrs.module.disa.api.sync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.api.CRAGLabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.SampleType;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;

public class CRAGLabResultHandlerTest extends BaseContextMockTest {
    @Mock
    private EncounterService encounterService;

    @Mock
    private FormService formService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private PersonService personService;

    @InjectMocks
    private CRAGLabResultHandler cragLabResultHandler;

    private CRAGLabResult labResult;

    private Concept sampleType;
    private Concept serum;
    private Concept dryBloodSpot;

    @Before
    public void setUp() {
        labResult = new CRAGLabResult();
        labResult.setLabResultStatus(LabResultStatus.PENDING);
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setNid("000000000/0000/00000");
        labResult.setFinalResult("Negativ");
        labResult.setHealthFacilityLabCode("1040107");
        labResult.setPregnant("");
        labResult.setBreastFeeding("");
        labResult.setReasonForTest("");
        labResult.setPrimeiraLinha("");
        labResult.setSegundaLinha("");
        labResult.setLabResultDate(LocalDateTime.now());
        labResult.setSampleType(SampleType.SER);

        Provider provider = new Provider();
        cragLabResultHandler.getSyncContext().put(ProviderLookup.PROVIDER_KEY, provider);

        Patient patient = new Patient();
        cragLabResultHandler.getSyncContext().put(PatientNidLookup.PATIENT_KEY, patient);

        Location location = new Location();
        location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
        cragLabResultHandler.getSyncContext().put(LocationLookup.LOCATION_KEY, location);

        sampleType = new Concept(23832);
        sampleType.setUuid(Constants.SAMPLE_TYPE);
        when(conceptService.getConceptByUuid(Constants.SAMPLE_TYPE))
                .thenReturn(sampleType);

        serum = new Concept(1001);
        serum.setUuid(SampleType.SER.getConceptUuid());
        when(conceptService.getConceptByUuid(SampleType.SER.getConceptUuid()))
                .thenReturn(serum);

        dryBloodSpot = new Concept(23831);
        dryBloodSpot.setUuid(SampleType.DBS.getConceptUuid());
        when(conceptService.getConceptByUuid(SampleType.DBS.getConceptUuid()))
                .thenReturn(dryBloodSpot);
    }

    @Test
    public void shouldNotProcessNullResults() {
        labResult.setFinalResult(null);
        cragLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldNotProcessEmptyResults() {
        labResult.setFinalResult("");
        cragLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldNotProcessUnexpectedResult() {
        labResult.setFinalResult("Random text");
        cragLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldProcessPositiveResult() {
        labResult.setFinalResult("Positiv12349abcd");
        cragLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

    @Test
    public void shouldHandleSampleType() throws Exception {

        labResult.setFinalResult("Positiv");

        cragLabResultHandler.handle(labResult);

        Encounter encounter = (Encounter) cragLabResultHandler.getSyncContext()
                .get(BaseLabResultHandler.ENCOUNTER_KEY);

        assertThat(encounter.getObs(),
                hasItem(allOf(
                        hasProperty("concept", equalTo(sampleType)),
                        hasProperty("valueCoded", equalTo(serum)))));
    }

    @Test
    public void shouldNotSaveInvalidSampleType() {

        labResult.setSampleType(SampleType.DBS);

        cragLabResultHandler.handle(labResult);

        Encounter encounter = (Encounter) cragLabResultHandler.getSyncContext()
                .get(BaseLabResultHandler.ENCOUNTER_KEY);

        assertThat(encounter.getObs(),
                not(hasItem(allOf(
                        hasProperty("concept", equalTo(sampleType)),
                        hasProperty("valueCoded", equalTo(dryBloodSpot))))));
    }
}

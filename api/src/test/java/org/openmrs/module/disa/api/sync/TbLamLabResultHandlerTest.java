package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.TBLamLabResult;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;

public class TbLamLabResultHandlerTest extends BaseContextMockTest {

    @Mock
    private EncounterService encounterService;

    @Mock
    private FormService formService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private PersonService personService;

    @InjectMocks
    private TBLamLabResultHandler tbLamLabResultHandler;

    private TBLamLabResult labResult;

    private Concept positivityLevel;
    private Concept level3;

    @Before
    public void setUp() {
        labResult = new TBLamLabResult();
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
        tbLamLabResultHandler.getSyncContext().put(ProviderLookup.PROVIDER_KEY, provider);

        Patient patient = new Patient();
        tbLamLabResultHandler.getSyncContext().put(PatientNidLookup.PATIENT_KEY, patient);

        Location location = new Location();
        location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
        tbLamLabResultHandler.getSyncContext().put(LocationLookup.LOCATION_KEY, location);

        positivityLevel = new Concept();
        positivityLevel.setUuid(Constants.POSITIVITY_LEVEL);

        level3 = new Concept();
        level3.setUuid(Constants.LEVEL_3);
    }

    @Test
    public void shouldNotProcessNullResults() {
        labResult.setFinalResult(null);
        tbLamLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldNotProcessEmptyResults() {
        labResult.setFinalResult("");
        tbLamLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldNotProcessUnexpectedResult() {
        labResult.setFinalResult("Random text");
        tbLamLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));
    }

    @Test
    public void shouldProcessNegativeResult() {
        labResult.setFinalResult("Negativ");
        tbLamLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

    @Test
    public void shouldProcessPositiveResult() {
        labResult.setFinalResult("Positiv");
        tbLamLabResultHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
    }

    @Test
    public void shouldProcessPositivityLevel() {

        when(conceptService.getConceptByUuid(Constants.POSITIVITY_LEVEL)).thenReturn(positivityLevel);
        when(conceptService.getConceptByUuid(Constants.LEVEL_3)).thenReturn(level3);

        labResult.setFinalResult("Positiv");
        labResult.setPositivityLevel("GRIII");
        tbLamLabResultHandler.handle(labResult);

        Encounter encounter = (Encounter) tbLamLabResultHandler.getSyncContext()
            .get(BaseLabResultHandler.ENCOUNTER_KEY);
        
        Obs positivityLevelObs = encounter.getObs().stream()
            .filter(o -> o.getConcept()!=null)
            .filter(o -> o.getConcept().equals(positivityLevel))
            .findFirst()
            .get();
        
        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
        assertThat(encounter, is(notNullValue()));
        assertThat(positivityLevelObs.getValueCoded(), is(level3));
    }
}

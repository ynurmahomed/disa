package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.test.BaseContextMockTest;

public class BaseLabResultHandlerTest extends BaseContextMockTest {

    @Mock
    private EncounterService encounterService;

    @Mock
    private FormService formService;

    @Mock
    protected PersonService personService;

    @Mock
    protected ConceptService conceptService;

    class MockLabResultHandler extends BaseLabResultHandler {

    };

    @InjectMocks
    private MockLabResultHandler baseLabResultHandler = new MockLabResultHandler();

    @Test
    public void shouldCreateEncounterWithAuthorizationdDate() {
        Patient patient = new Patient();
        Provider provider = new Provider();
        Location location = new Location();
        LocalDateTime authorizationDate = LocalDateTime.now();
        LabResult labResult = new HIVVLLabResult();
        labResult.setRequestId("MZDISAPQM0000000");
        labResult.setLabResultDate(authorizationDate);
        labResult.setPregnant("No");
        labResult.setBreastFeeding("No");

        Encounter elabEncounter = baseLabResultHandler.getElabEncounter(patient, provider, location, labResult);
        LocalDateTime encounterDateTime = elabEncounter.getEncounterDatetime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        assertThat(encounterDateTime.toLocalDate(), is(authorizationDate.toLocalDate()));
        assertThat(encounterDateTime.toLocalTime(), is(LocalTime.MIDNIGHT));
    }
}

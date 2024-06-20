package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.SampleType;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;

public class HIVVLLabResultHandlerTest extends BaseContextMockTest {

	@Mock
	private LabResultService labResultService;
	@Mock
	private LocationService locationService;
	@Mock
	private EncounterService encounterService;
	@Mock
	private FormService formService;
	@Mock
	private AdministrationService administrationService;
	@Mock
	private PersonService personService;
	@Mock
	private ConceptService conceptService;

	@Mock
	private LabResultHandler next;

	@InjectMocks
	private HIVVLLabResultHandler hivvlLabResultHandler;

	private HIVVLLabResult labResult;
	private Person person;
	private User user;
	private Provider provider;
	private String province;
	private EncounterType encounterType;
	private Patient patient;
	private Concept viralLoadCopies;
	private Concept viralLoadQualitative;
	private Concept lessThan;
	private Concept undetectable;
	private Concept labNumber;
	private Concept pickingLocation;
	private Concept recomendingEncounter;
	private Concept pregnant;
	private Concept orderId;
	private Concept sampleType;
	private Concept dryBloodSpot;
	private Concept serum;
	private Location location;

	@Before
	public void setUp() throws Exception {

		hivvlLabResultHandler.setNext(next);

		labResult = new HIVVLLabResult();
		labResult.setLabResultStatus(LabResultStatus.PENDING);
		labResult.setRequestId("MZDISAPQM0000000");
		labResult.setNid("000000000/0000/00000");
		labResult.setFinalResult("INDETECTAVEL");
		labResult.setHealthFacilityLabCode("1040107");
		labResult.setPregnant("");
		labResult.setBreastFeeding("");
		labResult.setReasonForTest("");
		labResult.setPrimeiraLinha("");
		labResult.setSegundaLinha("");
		labResult.setSampleType(SampleType.DBS);
		labResult.setLabResultDate(LocalDateTime.now());

		person = new Person();
		user = new User(person);
		province = "Zambézia";

		provider = new Provider();
		hivvlLabResultHandler.getSyncContext().put(ProviderLookup.PROVIDER_KEY, provider);

		encounterType = new EncounterType();
		when(encounterService.getEncounterTypeByUuid(anyString()))
				.thenReturn(encounterType);

		patient = new Patient();
		hivvlLabResultHandler.getSyncContext().put(PatientNidLookup.PATIENT_KEY, patient);

		location = new Location();
		location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
		hivvlLabResultHandler.getSyncContext().put(LocationLookup.LOCATION_KEY, location);

		Form form = new Form();
		when(formService.getFormByUuid(anyString()))
				.thenReturn(form);

		EncounterRole encounterRole = new EncounterRole();
		when(encounterService.getEncounterRoleByUuid(anyString()))
				.thenReturn(encounterRole);

		viralLoadCopies = new Concept(856);
		viralLoadCopies.setUuid(Constants.VIRAL_LOAD_COPIES);
		when(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES))
				.thenReturn(viralLoadCopies);

		viralLoadQualitative = new Concept(1305);
		viralLoadQualitative.setUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE);
		when(conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE))
				.thenReturn(viralLoadQualitative);

		lessThan = new Concept(165331);
		lessThan.setUuid(Constants.LESSTHAN);
		when(conceptService.getConceptByUuid(Constants.LESSTHAN))
				.thenReturn(lessThan);

		undetectable = new Concept(23814);
		undetectable.setUuid(Constants.UNDETECTABLE_VIRAL_LOAD);
		when(conceptService.getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD))
				.thenReturn(undetectable);

		labNumber = new Concept(23835);
		labNumber.setUuid(Constants.LAB_NUMBER);
		when(conceptService.getConceptByUuid(Constants.LAB_NUMBER))
				.thenReturn(labNumber);

		pickingLocation = new Concept(23883);
		pickingLocation.setUuid(Constants.PICKING_LOCATION);
		when(conceptService.getConceptByUuid(Constants.PICKING_LOCATION))
				.thenReturn(pickingLocation);

		recomendingEncounter = new Concept(23836);
		recomendingEncounter.setUuid(Constants.ENCOUNTER_SERVICE);
		when(conceptService.getConceptByUuid(Constants.ENCOUNTER_SERVICE))
				.thenReturn(recomendingEncounter);

		pregnant = new Concept(1982);
		pregnant.setUuid(Constants.PREGNANT);
		when(conceptService.getConceptByUuid(Constants.PREGNANT))
				.thenReturn(pregnant);

		orderId = new Concept(22771);
		orderId.setUuid(Constants.ORDER_ID);
		when(conceptService.getConceptByUuid(Constants.ORDER_ID))
				.thenReturn(orderId);

		sampleType = new Concept(23832);
		sampleType.setUuid(Constants.SAMPLE_TYPE);
		when(conceptService.getConceptByUuid(Constants.SAMPLE_TYPE))
				.thenReturn(sampleType);

		dryBloodSpot = new Concept(23831);
		dryBloodSpot.setUuid(SampleType.DBS.getConceptUuid());
		when(conceptService.getConceptByUuid(SampleType.DBS.getConceptUuid()))
				.thenReturn(dryBloodSpot);

		serum = new Concept(1001);
		serum.setUuid(SampleType.SER.getConceptUuid());
		when(conceptService.getConceptByUuid(SampleType.SER.getConceptUuid()))
				.thenReturn(serum);
	}

	@Test(expected = DisaModuleAPIException.class)
	public void shouldNotRunWithoutPatient() {
		hivvlLabResultHandler.getSyncContext().remove(PatientNidLookup.PATIENT_KEY);
		hivvlLabResultHandler.handle(labResult);
	}

	@Test(expected = DisaModuleAPIException.class)
	public void shouldNotRunWithoutLocation() {
		hivvlLabResultHandler.getSyncContext().remove(LocationLookup.LOCATION_KEY);
		hivvlLabResultHandler.handle(labResult);
	}

	@Test(expected = DisaModuleAPIException.class)
	public void shouldNotRunWithoutGenericProvider() {
		hivvlLabResultHandler.getSyncContext().remove(ProviderLookup.PROVIDER_KEY);
		hivvlLabResultHandler.handle(labResult);
	}

	@Test
	public void shouldSetTheLabResultToPROCESSED() {
		hivvlLabResultHandler.handle(labResult);
		assertThat(labResult.getLabResultStatus(), is(LabResultStatus.PROCESSED));
	}

	@Test
	public void shouldSetStatusToNotProcessedNoResultAndUpdateTheResult() {

		labResult.setFinalResult("");

		hivvlLabResultHandler.handle(labResult);

		assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
		assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldSetStatusToNotProcessedInvalidResultAndUpdateTheResult() {

		labResult.setFinalResult("ABCDEFGH");

		hivvlLabResultHandler.handle(labResult);

		assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
		assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleIndetectavel() {

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be indetectável
		assertThat(encounter.getObs(),
				hasItem(both(hasProperty("concept", equalTo(viralLoadQualitative)))
						.and(hasProperty("valueCoded", equalTo(undetectable)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);

	}

	@Test
	public void shouldHandleNumericValues() {

		labResult.setFinalResult(" 123456     ");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should have finalViralLoadResult value
		assertThat(encounter.getObs(),
				hasItem(both(hasProperty("concept", equalTo(viralLoadCopies)))
						.and(hasProperty("valueNumeric", equalTo(123456.0)))));

		// Viral load qualitative should be empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleLessThanValues() throws Exception {

		labResult.setFinalResult("< 400 copias / ml");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be '<'
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadQualitative)),
						hasProperty("valueCoded", equalTo(lessThan)),
						hasProperty("comment", equalTo("400")))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleLessThanCpMl() throws Exception {

		labResult.setFinalResult("< 400 CP / mL");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be '<'
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadQualitative)),
						hasProperty("valueCoded", equalTo(lessThan)),
						hasProperty("comment", equalTo("400")))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleLessThanCopiesMl() throws Exception {

		labResult.setFinalResult("< 400 CoPiEs / Ml");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be '<'
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadQualitative)),
						hasProperty("valueCoded", equalTo(lessThan)),
						hasProperty("comment", equalTo("400")))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldNotSaveMoreThanValuesWithInvalidNumericValues() throws Exception {

		labResult.setFinalResult("> 10.000.000,00");

		hivvlLabResultHandler.handle(labResult);

		verify(encounterService, never()).saveEncounter(anyObject());

		assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
		assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldSetResultsWithMoreThanValuesWithInvalidNumericValuesAsInvalidResult() throws Exception {

		labResult.setFinalResult("> 10.000.000,00");

		hivvlLabResultHandler.handle(labResult);

		assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
		assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.INVALID_RESULT));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleMoreThanValues() throws Exception {

		labResult.setFinalResult("> 10000000");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be the numeric value
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadCopies)),
						hasProperty("valueNumeric", equalTo(10_000_000.0)))));

		// Viral load qualitative should empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleMoreThanValuesWithCpMl() throws Exception {

		labResult.setFinalResult("> 10000000 cp/ml");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be the numeric value
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadCopies)),
						hasProperty("valueNumeric", equalTo(10_000_000.0)))));

		// Viral load qualitative should empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleMoreThanValuesWithCopiesMl() throws Exception {

		labResult.setFinalResult("> 10000000 copies / ml");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be the numeric value
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadCopies)),
						hasProperty("valueNumeric", equalTo(10_000_000.0)))));

		// Viral load qualitative should empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldHandleMoreThanValuesWithCopiasMl() throws Exception {

		labResult.setFinalResult("> 10000000 copias / ml");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getEncounterType(), is(encounterType));
		assertThat(encounter.getPatient(), is(patient));

		// Viral load copies should be the numeric value
		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadCopies)),
						hasProperty("valueNumeric", equalTo(10_000_000.0)))));

		// Viral load qualitative should empty
		assertThat(encounter.getObs(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));

		// calls next handler
		verify(next, times(1)).handle(labResult);
	}

	@Test
	public void shouldCreateEncounterAtStartOfDay() throws Exception {

		labResult.setFinalResult(" 123456     ");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		Date date = encounter.getEncounterDatetime();
		LocalDateTime encounterDateTime = Instant.ofEpochMilli(date.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		assertThat(encounterDateTime.getHour(), is(0));
		assertThat(encounterDateTime.getMinute(), is(0));
		assertThat(encounterDateTime.getSecond(), is(0));
	}

	@Test
	public void shouldHandleSampleType() throws Exception {

		labResult.setFinalResult("INDETECTAVEL");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getObs(),
				hasItem(allOf(
						hasProperty("concept", equalTo(sampleType)),
						hasProperty("valueCoded", equalTo(dryBloodSpot)))));
	}

	@Test
	public void shouldNotSaveInvalidSampleType() {

		labResult.setSampleType(SampleType.SER);
		labResult.setFinalResult("INDETECTAVEL");

		hivvlLabResultHandler.handle(labResult);

		Encounter encounter = (Encounter) hivvlLabResultHandler.getSyncContext()
				.get(BaseLabResultHandler.ENCOUNTER_KEY);

		assertThat(encounter.getObs(),
				not(hasItem(allOf(
						hasProperty("concept", equalTo(sampleType)),
						hasProperty("valueCoded", equalTo(serum))))));
	}
}

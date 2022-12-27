package org.openmrs.module.disa.scheduler;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.test.BaseContextMockTest;

import com.google.gson.Gson;

public class ViralLoadFormSchedulerTaskTest extends BaseContextMockTest {

	@Mock
	private DisaService disaService;

	@Mock
	private AdministrationService administrationService;

	@Mock
	private UserService userService;

	@Mock
	private ProviderService providerService;

	@Mock
	private EncounterService encounterService;

	@Mock
	private PersonService personService;

	@Mock
	private ConceptService conceptService;

	@Mock
	private LocationService locationService;

	@Mock
	private ObsService obsService;

	// Used during Context.openSession()
	@Mock
	private ContextDAO contextDAO;

	@Mock
	private RestUtil restUtil;

	@Mock
	private FormService formService;

	@InjectMocks
	private ViralLoadFormSchedulerTask task;

	private Disa disa;
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
	private Location location;

	@Before
	public void setUp() throws Exception {
		disa = new Disa();
		disa.setRequestId("MZDISAPQM0000000");
		disa.setNid("000000000/0000/00000");
		disa.setFinalViralLoadResult("INDETECTAVEL");
		disa.setHealthFacilityLabCode("1040107");
		disa.setPregnant("");
		disa.setBreastFeeding("");
		disa.setReasonForTest("");
		disa.setPrimeiraLinha("");
		disa.setSegundaLinha("");

		person = new Person();
		user = new User(person);
		provider = new Provider();
		province = "Zambézia";

		encounterType = new EncounterType();
		when(encounterService.getEncounterTypeByUuid(anyString()))
				.thenReturn(encounterType);

		patient = new Patient();
		when(disaService.getPatientByPatientId(1))
				.thenReturn(Collections.singletonList(patient));

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		String uuid = "132895aa-1c88-11e8-b6fd-7395830b63f3";
		GlobalProperty locationAttrTypeGp = new GlobalProperty(Constants.LOCATION_ATTRIBUTE_TYPE_UUID, uuid);
		when(administrationService.getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID))
				.thenReturn(locationAttrTypeGp);

		LocationAttributeType locationAttrType = new LocationAttributeType();
		when(locationService.getLocationAttributeTypeByUuid(uuid))
				.thenReturn(locationAttrType);

		location = new Location();
		location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
		when(locationService.getLocations(
				isNull(String.class),
				isNull(Location.class),
				anyMapOf(LocationAttributeType.class, Object.class),
				eq(false),
				isNull(Integer.class),
				isNull(Integer.class)))
				.thenReturn(Collections.singletonList(location));

		Form form = new Form();
		when(formService.getFormByUuid(anyString()))
				.thenReturn(form);

		EncounterRole encounterRole = new EncounterRole();
		when(encounterService.getEncounterRoleByUuid(anyString()))
				.thenReturn(encounterRole);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

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

		when(locationService.getDefaultLocation())
			.thenReturn(location);
	}

	@Test
	public void executeShouldNotRunWithoutGenericProvider() throws Exception {

		when(userService.getUserByUsername(anyString())).thenReturn(null);

		task.execute();

		verify(encounterService, never()).saveEncounter(anyObject());

	}

	@Test
	public void executeShouldSetStatusToNotProcessed() throws Exception {

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.emptyList());

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "nid", location.getUuid());

	}

	@Test
	public void executeShouldSetStatusToNotProcessedDuplicatedNid() throws Exception {

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Arrays.asList(1, 2));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "duplicate", location.getUuid());

	}

	@Test
	public void executeShouldSetStatusToNotProcessedNoResult() throws Exception {

		disa.setFinalViralLoadResult("");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "result", location.getUuid());

	}

	@Test
	public void executeShouldSetStatusToNotProcessedFlaggedForReview() throws Exception {

		disa.setFinalViralLoadResult("ABCDEFGH");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "review", location.getUuid());

	}

	@Test
	public void executeShouldHandleIndetectavel() throws Exception {

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
		verify(encounterService, times(1))
				.saveEncounter(encounterCaptor.capture());

		assertThat(encounterCaptor.getValue().getEncounterType(), is(encounterType));
		assertThat(encounterCaptor.getValue().getPatient(), is(patient));

		ArgumentCaptor<Obs> obsCaptor = ArgumentCaptor.forClass(Obs.class);
		verify(obsService, atLeast(2))
				.saveObs(obsCaptor.capture(), eq(""));

		// Viral load copies should be empty
		assertThat(obsCaptor.getAllValues(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be indetectável
		assertThat(obsCaptor.getAllValues(),
				hasItem(both(hasProperty("concept", equalTo(viralLoadQualitative)))
						.and(hasProperty("valueCoded", equalTo(undetectable)))));
	}

	@Test
	public void executeShouldHandleNumericValues() throws Exception {

		disa.setFinalViralLoadResult(" 123456     ");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
		verify(encounterService, times(1))
				.saveEncounter(encounterCaptor.capture());

		assertThat(encounterCaptor.getValue().getEncounterType(), is(encounterType));
		assertThat(encounterCaptor.getValue().getPatient(), is(patient));

		ArgumentCaptor<Obs> obsCaptor = ArgumentCaptor.forClass(Obs.class);
		verify(obsService, atLeast(2))
				.saveObs(obsCaptor.capture(), eq(""));

		// Viral load copies should have finalViralLoadResult value
		assertThat(obsCaptor.getAllValues(),
				hasItem(both(hasProperty("concept", equalTo(viralLoadCopies)))
						.and(hasProperty("valueNumeric", equalTo(123456.0)))));

		// Viral load qualitative should be empty
		assertThat(obsCaptor.getAllValues(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));
	}

	@Test
	public void executeShouldHandleLessThanValues() throws Exception {

		disa.setFinalViralLoadResult("< 400 copias / ml");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
		verify(encounterService, times(1))
				.saveEncounter(encounterCaptor.capture());

		assertThat(encounterCaptor.getValue().getEncounterType(), is(encounterType));
		assertThat(encounterCaptor.getValue().getPatient(), is(patient));

		ArgumentCaptor<Obs> obsCaptor = ArgumentCaptor.forClass(Obs.class);
		verify(obsService, atLeast(2))
				.saveObs(obsCaptor.capture(), eq(""));

		// Viral load copies should be empty
		assertThat(obsCaptor.getAllValues(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadCopies)))));

		// Viral load qualitative should be '<'
		assertThat(obsCaptor.getAllValues(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadQualitative)),
						hasProperty("valueCoded", equalTo(lessThan)),
						hasProperty("comment", equalTo("400")))));
	}

	@Test
	public void executeShouldHandleMoreThanValues() throws Exception {

		disa.setFinalViralLoadResult("> 10000000");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
		verify(encounterService, times(1))
				.saveEncounter(encounterCaptor.capture());

		assertThat(encounterCaptor.getValue().getEncounterType(), is(encounterType));
		assertThat(encounterCaptor.getValue().getPatient(), is(patient));

		ArgumentCaptor<Obs> obsCaptor = ArgumentCaptor.forClass(Obs.class);
		verify(obsService, atLeast(2))
				.saveObs(obsCaptor.capture(), eq(""));

		// Viral load copies should be the numeric value
		assertThat(obsCaptor.getAllValues(),
				hasItem(allOf(
						hasProperty("concept", equalTo(viralLoadCopies)),
						hasProperty("valueNumeric", equalTo(10_000_000.0)))));

		// Viral load qualitative should empty
		assertThat(obsCaptor.getAllValues(),
				not(hasItem(hasProperty("concept", equalTo(viralLoadQualitative)))));
	}

	@Test
	public void executeShouldCreateEncounterAtStartOfDay() throws Exception {

		disa.setFinalViralLoadResult(" 123456     ");
		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		when(userService.getUserByUsername(anyString())).thenReturn(user);

		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		ArgumentCaptor<Encounter> encounterCaptor = ArgumentCaptor.forClass(Encounter.class);
		verify(encounterService, times(1))
				.saveEncounter(encounterCaptor.capture());

		Date date = encounterCaptor.getValue().getEncounterDatetime();
		LocalDateTime encounterDateTime = Instant.ofEpochMilli(date.getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		assertThat(encounterDateTime.getHour(), is(0));
		assertThat(encounterDateTime.getMinute(), is(0));
		assertThat(encounterDateTime.getSecond(), is(0));
	}
}

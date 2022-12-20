package org.openmrs.module.disa.scheduler;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
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

	@Before
	public void setUp() {
		disa = new Disa();
		disa.setRequestId("MZDISAPQM0000000");
		disa.setNid("000000000/0000/00000");
		disa.setFinalViralLoadResult("INDETECTAVEL");
		disa.setHealthFacilityLabCode("1040107");
	}

	@Test
	public void executeShouldNotRunWithoutGenericProvider() {

		when(userService.getUserByUsername(anyString())).thenReturn(null);

		task.execute();

		verify(encounterService, never()).saveEncounter(anyObject());

	}

	@Test
	public void executeShouldSetStatusToNotProcessed() throws Exception {

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		String province = "Zambézia";
		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		Person person = new Person();
		User user = new User(person);
		when(userService.getUserByUsername(anyString())).thenReturn(user);

		Provider provider = new Provider();
		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.emptyList());

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "nid");

	}

	@Test
	public void executeShouldSetStatusToNotProcessedDuplicatedNid() throws Exception {

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		String province = "Zambézia";
		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		Person person = new Person();
		User user = new User(person);
		when(userService.getUserByUsername(anyString())).thenReturn(user);

		Provider provider = new Provider();
		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Arrays.asList(1, 2));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "duplicate");

	}

	@Test
	public void executeShouldSetStatusToNotProcessedNoResult() throws Exception {

		disa.setFinalViralLoadResult("");

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		String province = "Zambézia";
		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		Person person = new Person();
		User user = new User(person);
		when(userService.getUserByUsername(anyString())).thenReturn(user);

		Provider provider = new Provider();
		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "result");

	}

	@Test
	public void executeShouldSetStatusToNotProcessedFlaggedForReview() throws Exception {

		disa.setFinalViralLoadResult("ABCDEFGH");

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		String province = "Zambézia";
		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		Person person = new Person();
		User user = new User(person);
		when(userService.getUserByUsername(anyString())).thenReturn(user);

		Provider provider = new Provider();
		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		task.execute();

		verify(restUtil, times(1))
				.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, disa.getRequestId(), "review");

	}

	@Test
	public void executeShouldCreateDisaEncounter() throws Exception {

		GlobalProperty sismaCodes = new GlobalProperty(Constants.DISA_SISMA_CODE, "1040100,1040106,1040107");
		when(administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE))
				.thenReturn(sismaCodes);

		String province = "Zambézia";
		GlobalProperty disaProvince = new GlobalProperty(Constants.DISA_PROVINCE, province);
		when(administrationService.getGlobalPropertyObject(Constants.DISA_PROVINCE))
				.thenReturn(disaProvince);

		when(restUtil.getRequestGet(anyListOf(String.class), eq(province)))
				.thenReturn(new Gson().toJson(new Disa[] { disa }));

		Person person = new Person();
		User user = new User(person);
		when(userService.getUserByUsername(anyString())).thenReturn(user);

		Provider provider = new Provider();
		when(providerService.getProvidersByPerson(person))
				.thenReturn(Collections.singleton(provider));

		when(disaService.existsByRequestId(disa.getRequestId()))
				.thenReturn(false);

		when(disaService.getPatientByNid(disa.getNid()))
				.thenReturn(Collections.singletonList(1));

		Patient patient = new Patient();
		when(disaService.getPatientByPatientId(1))
				.thenReturn(Collections.singletonList(patient));

		String uuid = "132895aa-1c88-11e8-b6fd-7395830b63f3";
		GlobalProperty locationAttrTypeGp = new GlobalProperty(Constants.LOCATION_ATTRIBUTE_TYPE_UUID, uuid);
		when(administrationService.getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID))
				.thenReturn(locationAttrTypeGp);

		LocationAttributeType locationAttrType = new LocationAttributeType();
		when(locationService.getLocationAttributeTypeByUuid(uuid))
				.thenReturn(locationAttrType);

		Location location = new Location();
		when(locationService.getLocations(
				isNull(String.class),
				isNull(Location.class),
				anyMapOf(LocationAttributeType.class, Object.class),
				eq(false),
				isNull(Integer.class),
				isNull(Integer.class)))
				.thenReturn(Collections.singletonList(location));

		EncounterType encounterType = new EncounterType();
		when(encounterService.getEncounterTypeByUuid(anyString()))
				.thenReturn(encounterType);

		Form form = new Form();
		when(formService.getFormByUuid(anyString()))
				.thenReturn(form);

		EncounterRole encounterRole = new EncounterRole();
		when(encounterService.getEncounterRoleByUuid(anyString()))
				.thenReturn(encounterRole);

		task.execute();

		verify(encounterService, times(1)).saveEncounter(anyObject());

	}
}

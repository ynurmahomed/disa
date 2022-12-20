package org.openmrs.module.disa.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
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
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.FsrLog;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.GenericUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author machabane
 *
 */
@Component
public class ViralLoadFormSchedulerTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ViralLoadFormSchedulerTask.class);

	private String processed;
	private String notProcessed;
	private String notProcessedNoResult;
	private String notProcessedFlaggedReview;
	private String notProcessedDuplicateNid;
	private RestUtil rest;
	private DisaService disaService;
	private Location locationBySismaCode;
	private List<Patient> lstPatient = new ArrayList<Patient>();
	private List<Disa> jsonViralLoad = new ArrayList<Disa>();
	private List<Integer> patientIds = new ArrayList<Integer>();
	private AdministrationService administrationService;
	private UserService userService;
	private ProviderService providerService;
	private EncounterService encounterService;
	private PersonService personService;
	private ConceptService conceptService;
	private LocationService locationService;
	private ObsService obsService;
	private FormService formService;

	@Autowired
	public ViralLoadFormSchedulerTask(
			DisaService disaService,
			@Qualifier("adminService") AdministrationService administrationService,
			@Qualifier("userService") UserService userService,
			@Qualifier("providerService") ProviderService providerService,
			@Qualifier("encounterService") EncounterService encounterService,
			@Qualifier("personService") PersonService personService,
			@Qualifier("conceptService") ConceptService conceptService,
			@Qualifier("locationService") LocationService locationService,
			@Qualifier("obsService") ObsService obsService,
			@Qualifier("formService") FormService formService) {

		this.administrationService = administrationService;
		this.userService = userService;
		this.providerService = providerService;
		this.encounterService = encounterService;
		this.personService = personService;
		this.conceptService = conceptService;
		this.locationService = locationService;
		this.obsService = obsService;
		this.disaService = disaService;
		this.formService = formService;
	}

	@PostConstruct
	public void postConstruct() {
		rest = new RestUtil();
		rest.setURLBase(
				administrationService.getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				administrationService.getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				administrationService.getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
	}

	@Override
	public void execute() {
		logger.info("module started...");
		Context.openSession();
		try {
			createViralLoadForm();
		} catch (HttpHostConnectException e) {
			// ignora a exception
		} catch (Exception e) {
			logger.error("O erro ", e);
			// sendEmail(exceprionMessage,
			// administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_TO).getPropertyValue());
		} finally {

		}
		Context.closeSession();
		logger.info("module ended...");
	}

	@Transactional(rollbackFor = Exception.class)
	private void createViralLoadForm() throws Exception, HttpHostConnectException {

		// iterate the viral load list and create the encounters

		jsonViralLoad = getJsonViralLoad();
		logger.info("There is {} pending items to be processed", jsonViralLoad.size() );

		logger.info("Syncing started...");

		User user = userService.getUserByUsername("generic.provider");
		if (user == null) {
			user = userService.getUserByUsername("provedor.desconhecido");
		}

		if (user == null) {
			logger.error("O erro O provedor generic.provider ou provedor.desconhecido nao foi encontrado no openmrs.");
			// sendEmail(message,
			// administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_TO).getPropertyValue());
			return;
		}

		Provider provider = providerService.getProvidersByPerson(user.getPerson()).iterator().next();

		for (Disa disa : jsonViralLoad) {

			if (!disaService.existsByRequestId(disa.getRequestId())) {

				Encounter encounter = new Encounter();
				encounter.setEncounterDatetime(DateUtil.dateWithLeadingZeros());

				patientIds = disaService.getPatientByNid(disa.getNid().trim());

				if (patientIds.isEmpty()) {
					notProcessed = disa.getRequestId();
					updateNotProcessed();
					continue;
				} else {
					if (patientIds.size() > 1) {
						// notify duplication
						String notification = "Os pacientes do OpenMRS com os Ids: "
								+ Arrays.toString(patientIds.toArray()) + " partilham o mesmo NID: "
								+ disa.getNid().trim();
						logger.info("O erro {}", notification);
						// sendEmail(notification,
						// administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_OTHERS_TO).getPropertyValue());
						notProcessedDuplicateNid = disa.getRequestId();
						updateNotProcessedDuplicateNid();
						continue;
					} else if (hasNoResult(disa)) {
						notProcessedNoResult = disa.getRequestId();
						updateNotProcessedNoResult();
						continue;
					} else if (!GenericUtil.isNumeric(disa.getFinalViralLoadResult().trim())
							&& !(disa.getFinalViralLoadResult().contains(Constants.LESS_THAN))
							&& !(disa.getFinalViralLoadResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL))) {
						notProcessedFlaggedReview = disa.getRequestId();
						updateNotProcessedFlaggedForReview();
						continue;
					} else {
						processed = disa.getRequestId();
						lstPatient = disaService.getPatientByPatientId(patientIds.get(0));
						encounter.setPatient(lstPatient.get(0));
					}
				}

				locationBySismaCode = getLocationBySismaCode(disa.getHealthFacilityLabCode());
				encounter.setEncounterType(
						encounterService.getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE));
				encounter.setLocation(locationBySismaCode);
				encounter.setForm(formService.getFormByUuid(Constants.DISA_FORM));

				encounter.setProvider(
						encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID),
						provider);

				encounterService.saveEncounter(encounter);

				Obs obs_23835 = new Obs();
				obs_23835.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23835.setObsDatetime(new Date());
				obs_23835.setConcept(conceptService.getConceptByUuid(Constants.LAB_NUMBER));
				obs_23835.setLocation(locationBySismaCode);
				obs_23835.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getHealthFacilityLabCode())) {// RequestingFacilityCode
					obs_23835.setValueText(disa.getHealthFacilityLabCode());
					obsService.saveObs(obs_23835, "");
				}

				Obs obs_23883 = new Obs();
				obs_23883.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23883.setObsDatetime(new Date());
				obs_23883.setConcept(conceptService.getConceptByUuid(Constants.PICKING_LOCATION));
				obs_23883.setLocation(locationBySismaCode);
				obs_23883.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getRequestingFacilityName())) {// RequestingFacilityName
					obs_23883.setValueText(disa.getRequestingFacilityName());
					obsService.saveObs(obs_23883, "");
				}

				Obs obs_23836 = new Obs();
				obs_23836.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23836.setObsDatetime(new Date());
				obs_23836.setConcept(conceptService.getConceptByUuid(Constants.ENCOUNTER_SERVICE));
				obs_23836.setLocation(locationBySismaCode);
				obs_23836.setEncounter(encounter);
				if (!(disa.getEncounter() == null)
						&& StringUtils.isNotEmpty(GenericUtil.wardSelection(disa.getEncounter().trim()))) {// WARD
					obs_23836.setValueCoded(conceptService
							.getConceptByName(GenericUtil.wardSelection(disa.getEncounter().trim())));
					obsService.saveObs(obs_23836, "");
				}

				Obs obs_1982 = new Obs();
				obs_1982.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_1982.setObsDatetime(new Date());
				obs_1982.setConcept(conceptService.getConceptByUuid(Constants.PREGNANT));
				obs_1982.setLocation(locationBySismaCode);
				obs_1982.setEncounter(encounter);
				if (disa.getPregnant().trim().equalsIgnoreCase(Constants.YES)
						|| disa.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// Pregnant
					obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.GESTATION));
					obsService.saveObs(obs_1982, "");
				} else if (disa.getPregnant().trim().equalsIgnoreCase(Constants.NO)
						|| StringUtils.stripAccents(disa.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
					obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
					obsService.saveObs(obs_1982, "");
				} else {
					obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					obsService.saveObs(obs_1982, "");
				}

				Obs obs_6332 = new Obs();
				obs_6332.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_6332.setObsDatetime(new Date());
				obs_6332.setConcept(conceptService.getConceptByUuid(Constants.LACTATION));
				obs_6332.setLocation(locationBySismaCode);
				obs_6332.setEncounter(encounter);
				if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
						|| disa.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// BreastFeeding
					obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_YES));
					obsService.saveObs(obs_6332, "");
				} else if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)
						|| StringUtils.stripAccents(disa.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
					obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
					obsService.saveObs(obs_6332, "");
				} else {
					obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					obsService.saveObs(obs_6332, "");
				}

				Obs obs_23818 = new Obs();
				obs_23818.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23818.setObsDatetime(new Date());
				obs_23818.setConcept(conceptService.getConceptByUuid(Constants.REASON_FOR_TEST));
				obs_23818.setLocation(locationBySismaCode);
				obs_23818.setEncounter(encounter);
				if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {// ReasonForTest
					obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.ROUTINE_VIRAL_LOAD));
					obsService.saveObs(obs_23818, "");
				} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) {
					obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.REGIMEN_FAILURE));
					obsService.saveObs(obs_23818, "");
				} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.RAB)) {
					obs_23818.setValueCoded(
							conceptService.getConceptByUuid(Constants.REPEAT_AFTER_BREASTFEEDING));
					obsService.saveObs(obs_23818, "");
				} else {
					obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					obsService.saveObs(obs_23818, "");
				}

				Obs obs_23821 = new Obs();
				obs_23821.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23821.setObsDatetime(new Date());
				obs_23821.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
				obs_23821.setLocation(locationBySismaCode);
				obs_23821.setEncounter(encounter);
				if (!(disa.getHarvestDate() == null) && StringUtils.isNotEmpty(disa.getHarvestDate())) {// SpecimenDatetime
					obs_23821.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
					obsService.saveObs(obs_23821, "");
				}

				Obs obs_23824 = new Obs();
				obs_23824.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23824.setObsDatetime(new Date());
				obs_23824.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD));
				obs_23824.setLocation(locationBySismaCode);
				obs_23824.setEncounter(encounter);
				if (!(disa.getHarvestType() == null)) {
					if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PV)) {// TypeOfSampleCollection
						obs_23824
								.setValueCoded(conceptService.getConceptByUuid(Constants.VENOUS_PUNCTURE));
						obsService.saveObs(obs_23824, "");
					} else if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PD)) {
						obs_23824.setValueCoded(
								conceptService.getConceptByUuid(Constants.DIGITAL_PUNCTURE));
						obsService.saveObs(obs_23824, "");
					}
				}

				Obs obs_23826 = new Obs();
				obs_23826.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23826.setObsDatetime(new Date());
				obs_23826.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
				obs_23826.setLocation(locationBySismaCode);
				obs_23826.setEncounter(encounter);
				if (!(disa.getDateOfSampleReceive() == null) && StringUtils.isNotEmpty(disa.getDateOfSampleReceive())) {// ReceivedDateTime
					obs_23826.setValueDate(DateUtil.deserialize(disa.getDateOfSampleReceive()));
					obsService.saveObs(obs_23826, "");
				}

				Obs obs_23833 = new Obs();
				obs_23833.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23833.setObsDatetime(new Date());
				obs_23833.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
				obs_23833.setLocation(locationBySismaCode);
				obs_23833.setEncounter(encounter);
				if (!(disa.getProcessingDate() == null) && StringUtils.isNotEmpty(disa.getProcessingDate())) {// AnalysisDateTime
					obs_23833.setValueDate(DateUtil.deserialize(disa.getProcessingDate()));
					obsService.saveObs(obs_23833, "");
				}

				// LastViralLoadResult & LastViralLoadDate
				Obs obs_165314 = new Obs();
				obs_165314.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_165314.setObsDatetime(new Date());
				obs_165314.setConcept(conceptService.getConceptByUuid(Constants.LAST_VIRALLOAD_RESULT));
				obs_165314.setLocation(locationBySismaCode);
				obs_165314.setEncounter(encounter);
				obs_165314.setValueText(disa.getLastViralLoadResult());
				if (DateUtil.isValidDate(disa.getLastViralLoadDate())) {
					obs_165314.setObsDatetime((DateUtil.string_To_Date(disa.getLastViralLoadDate())));
					obsService.saveObs(obs_165314, "");
				}

				// PrimeiraLinha & SegundaLinha
				Obs obs_21151 = new Obs();
				obs_21151.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_21151.setObsDatetime(new Date());
				obs_21151.setConcept(conceptService.getConceptByUuid(Constants.LINHA_TERAPEUTICA));
				obs_21151.setLocation(locationBySismaCode);
				obs_21151.setEncounter(encounter);
				if (disa.getPrimeiraLinha().equalsIgnoreCase(Constants.SIM)) {
					obs_21151.setValueCoded(conceptService.getConceptByUuid(Constants.PRIMEIRA_LINHA));
					obsService.saveObs(obs_21151, "");
				} else if (disa.getSegundaLinha().equalsIgnoreCase(Constants.SIM)) {
					obs_21151.setValueCoded(conceptService.getConceptByUuid(Constants.SEGUNDA_LINHA));
					obsService.saveObs(obs_21151, "");
				}

				// ARTRegimen
				Obs obs_165315 = new Obs();
				obs_165315.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_165315.setObsDatetime(new Date());
				obs_165315.setConcept(conceptService.getConceptByUuid(Constants.ART_REGIMEN));
				obs_165315.setLocation(locationBySismaCode);
				obs_165315.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getArtRegimen())) {
					obs_165315.setValueText(disa.getArtRegimen());
					obsService.saveObs(obs_165315, "");
				}

				// DataDeInicioDoTARV
				Obs obs_1190 = new Obs();
				obs_1190.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_1190.setObsDatetime(new Date());
				obs_1190.setConcept(conceptService.getConceptByUuid(Constants.ART_START_DATE));
				obs_1190.setLocation(locationBySismaCode);
				obs_1190.setEncounter(encounter);
				if (DateUtil.isValidDate(disa.getDataDeInicioDoTARV())) {
					obs_1190.setValueDatetime(DateUtil.string_To_Date(disa.getDataDeInicioDoTARV()));
					obsService.saveObs(obs_1190, "");
				}

				// SpecimenDatetime
				Obs obs_23840 = new Obs();
				obs_23840.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23840.setObsDatetime(new Date());
				obs_23840.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_REQUEST_DATE));
				obs_23840.setLocation(locationBySismaCode);
				obs_23840.setEncounter(encounter);
				if (!(disa.getHarvestDate() == null) && StringUtils.isNotEmpty(disa.getHarvestDate())) {
					obs_23840.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
					obsService.saveObs(obs_23840, "");
				}

				// LIMSSpecimenSourceCode
				Obs obs_23832 = new Obs();
				obs_23832.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23832.setObsDatetime(new Date());
				obs_23832.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_TYPE));
				obs_23832.setLocation(locationBySismaCode);
				obs_23832.setEncounter(encounter);
				if (!(disa.getSampleType() == null)
						&& disa.getSampleType().trim().equalsIgnoreCase(Constants.DRYBLOODSPOT)) {
					obs_23832.setValueCoded(conceptService.getConceptByUuid(Constants.DRY_BLOOD_SPOT));
					obsService.saveObs(obs_23832, "");
				} else if (!(disa.getSampleType() == null)
						&& disa.getSampleType().trim().equalsIgnoreCase(Constants.PLASMA)) {
					obs_23832.setValueCoded(conceptService.getConceptByUuid(Constants.PLASMA_));
					obsService.saveObs(obs_23832, "");
				}

				// Using FinalViralLoadResult column only
				// field: Cópias/ml
				if (GenericUtil.isNumeric(disa.getFinalViralLoadResult().trim())) {
					Obs obs_856 = new Obs();
					obs_856.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
					obs_856.setObsDatetime(new Date());
					obs_856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
					obs_856.setLocation(locationBySismaCode);
					obs_856.setEncounter(encounter);
					obs_856.setValueNumeric(Double.valueOf(disa.getFinalViralLoadResult().trim()));
					obsService.saveObs(obs_856, "");
				} else

				// field: dropbox with answer label Indectetável
				if (disa.getFinalViralLoadResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL)) {
					Obs obs_1306 = new Obs();
					obs_1306.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
					obs_1306.setObsDatetime(new Date());
					obs_1306.setConcept(
							conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
					obs_1306.setValueCoded(
							conceptService.getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD));
					obs_1306.setLocation(locationBySismaCode);
					obs_1306.setEncounter(encounter);
					obsService.saveObs(obs_1306, "");
				} else

				// field: dropbox with answer <
				if (disa.getFinalViralLoadResult().contains(Constants.LESS_THAN)) {
					Obs obs_1306 = new Obs();
					obs_1306.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
					obs_1306.setObsDatetime(new Date());
					obs_1306.setConcept(
							conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
					obs_1306.setValueCoded(conceptService.getConceptByUuid(Constants.LESSTHAN));
					obs_1306.setLocation(locationBySismaCode);
					obs_1306.setEncounter(encounter);
					obs_1306.setComment(disa.getFinalViralLoadResult()
							.trim()
							.substring(1)
							.replace(Constants.LESS_THAN, "")
							.replace(Constants.COPIES, "")
							.replace(Constants.FORWARD_SLASH, "")
							.replace(Constants.ML, "")
							.trim());
					obsService.saveObs(obs_1306, "");
				}

				Obs obs_23839 = new Obs();
				obs_23839.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23839.setObsDatetime(new Date());
				obs_23839.setConcept(conceptService.getConceptByUuid(Constants.APPROVED_BY));
				obs_23839.setEncounter(encounter);
				obs_23839.setLocation(locationBySismaCode);
				if (!(disa.getAprovedBy() == null) && StringUtils.isNotEmpty(disa.getAprovedBy().trim())) {
					obs_23839.setValueText(disa.getAprovedBy().trim());
					obsService.saveObs(obs_23839, "");
				}

				Obs obs_23841 = new Obs();
				obs_23841.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23841.setObsDatetime(new Date());
				obs_23841.setConcept(conceptService.getConceptByUuid(Constants.LAB_COMMENTS));
				obs_23841.setLocation(locationBySismaCode);
				obs_23841.setEncounter(encounter);
				if (!(disa.getLabComments() == null) && StringUtils.isNotEmpty(disa.getLabComments().trim())) {
					obs_23841.setValueText(disa.getLabComments().trim());
					obsService.saveObs(obs_23841, "");
				}

				Obs obs_22771 = new Obs();
				obs_22771.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_22771.setObsDatetime(new Date());
				obs_22771.setConcept(conceptService.getConceptByUuid(Constants.ORDER_ID));
				obs_22771.setLocation(locationBySismaCode);
				obs_22771.setEncounter(encounter);
				if (!(disa.getRequestId() == null) && StringUtils.isNotEmpty(disa.getRequestId().trim())) {
					obs_22771.setValueText(disa.getRequestId().trim());
					obsService.saveObs(obs_22771, "");
				}

				// log the fsr in openmrs
				FsrLog fsrLog = new FsrLog();
				fsrLog.setPatientId(encounter.getPatient().getPatientId());
				fsrLog.setEncounterId(encounter.getEncounterId());
				fsrLog.setPatientIdentifier(disa.getNid());
				fsrLog.setRequestId(disa.getRequestId());
				fsrLog.setCreator(Context.getAuthenticatedUser().getId());
				fsrLog.setDateCreated(new Date());
				disaService.saveFsrLog(fsrLog);

				updateProcessed();
			}
		}

		lstPatient.clear();
		jsonViralLoad.clear();
		patientIds.clear();

		logger.info("Syncing ended...");
	}

	private boolean hasNoResult(Disa disa) {
		return (disa.getFinalViralLoadResult() == null || disa.getFinalViralLoadResult().isEmpty());
	}

	private List<Disa> getJsonViralLoad() {
		String jsonViralLoadInfo = null;

		try {
			jsonViralLoadInfo = rest.getRequestGet(getAllDisaSismaCodes(), getDisaProvince());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {
		}.getType());
	}

	private void updateProcessed() throws Exception {
		try {
			rest.getRequestPutProcessed(Constants.URL_PATH_PROCESSED, processed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessed() throws Exception {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessed, "nid");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessedNoResult() throws Exception {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessedNoResult, "result");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessedFlaggedForReview() throws Exception {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessedFlaggedReview, "review");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessedDuplicateNid() {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessedDuplicateNid, "duplicate");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Location getLocationBySismaCode(String sismaCode) {
		LocationAttributeType locationAttributeType = locationService
				.getLocationAttributeTypeByUuid(administrationService
						.getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID).getPropertyValue());
		Map<LocationAttributeType, Object> hashMap = new HashMap<LocationAttributeType, Object>();
		hashMap.put(locationAttributeType, sismaCode);
		List<Location> locations = locationService.getLocations(null, null, hashMap, false, null, null);

		return locations.get(0);
	}

	private List<String> getAllDisaSismaCodes() {
		List<String> sismaCodes = Arrays.asList(administrationService
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue().split(","));
		return sismaCodes;
	}

	private String getDisaProvince() {
		return administrationService
				.getGlobalPropertyObject(Constants.DISA_PROVINCE).getPropertyValue();
	}

	@SuppressWarnings("unused")
	private void sendEmail(final String message, final String mailTo) {
		/*
		 * GenericUtil.SendMail(mailTo,
		 * administrationService.getGlobalPropertyObject(Constants.
		 * DISA_MAIL_FROM).getPropertyValue(),
		 * Constants.DISA_MAIL_SUBJECT
		 * +locationService.getDefaultLocation().getDescription(),
		 * Constants.DISA_MAIL_ERROR
		 * +new SimpleDateFormat("dd/MM/yyyy").format(new Date())+"\n\n\n" + message,
		 * administrationService.getGlobalPropertyObject(Constants.
		 * DISA_MAIL_HOST).getPropertyValue(),
		 * administrationService.getGlobalPropertyObject(Constants.
		 * DISA_MAIL_FROM_PASSWORD).getPropertyValue(),
		 * administrationService.getGlobalPropertyObject(Constants.
		 * DISA_MAIL_FROM_PORT).getPropertyValue());
		 */

		GenericUtil.sendMail(
				mailTo.split(","),
				Constants.DISA_MAIL_SUBJECT + locationService.getDefaultLocation().getDescription(),
				message,
				administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_FROM).getPropertyValue(),
				administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_HOST).getPropertyValue(),
				administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PORT)
						.getPropertyValue(),
				administrationService.getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PASSWORD)
						.getPropertyValue());
	}

	public void setRestUtil(RestUtil rest) {
		this.rest = rest;
	}
}
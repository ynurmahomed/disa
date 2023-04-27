package org.openmrs.module.disa.scheduler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.disa.FsrLog;
import org.openmrs.module.disa.HIVVLLabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.TypeOfResult;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.GenericUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author machabane
 *
 */
public class ViralLoadFormSchedulerTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ViralLoadFormSchedulerTask.class);

	private String processed;
	private String notProcessed;
	private String notProcessedNoResult;
	private String notProcessedFlaggedReview;
	private String notProcessedDuplicateNid;
	private String defaultLocationUuid;
	private RestUtil rest;
	private DisaService disaService;
	private Location locationBySismaCode;
	private List<Patient> lstPatient = new ArrayList<Patient>();
	private List<LabResult> jsonViralLoad = new ArrayList<LabResult>();
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
	private Gson gson;

	public ViralLoadFormSchedulerTask() {
		this.administrationService = Context.getAdministrationService();
		this.userService = Context.getUserService();
		this.providerService = Context.getProviderService();
		this.encounterService = Context.getEncounterService();
		this.personService = Context.getPersonService();
		this.conceptService = Context.getConceptService();
		this.locationService = Context.getLocationService();
		this.obsService = Context.getObsService();
		this.disaService = Context.getService(DisaService.class);
		this.formService = Context.getFormService();
		this.gson = Context.getRegisteredComponents(Gson.class).get(0);
		postConstruct();
	}

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
			@Qualifier("formService") FormService formService,
			Gson gson) {

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
		this.gson = gson;
	}

	public void setRestUtil(RestUtil rest) {
		this.rest = rest;
	}

	public void postConstruct() {
		rest = new RestUtil();
		rest.setURLBase(
				administrationService.getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				administrationService.getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				administrationService.getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
		rest.setGson(gson);
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
		logger.info("There is {} pending items to be processed", jsonViralLoad.size());

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

		defaultLocationUuid = locationService.getDefaultLocation().getUuid();

		int eLabsCreated = 0;
		int resultsEvaluated = 0;
		for (LabResult disa : jsonViralLoad) {

			logger.debug("Processing RequestId {}", disa.getRequestId());

			if (disa.getTypeOfResult() == TypeOfResult.HIVVL && !disaService.existsByRequestId(disa.getRequestId())) {

				HIVVLLabResult vl = (HIVVLLabResult) disa;

				Encounter encounter = new Encounter();
				Date dateWithLeadingZeros = DateUtil.dateWithLeadingZeros();
				encounter.setEncounterDatetime(dateWithLeadingZeros);

				patientIds = disaService.getPatientByNid(disa.getNid().trim());

				if (patientIds.isEmpty()) {
					notProcessed = disa.getRequestId();
					updatelabResultStatus(notProcessed, Constants.NID);
					resultsEvaluated++;
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
						updatelabResultStatus(notProcessedDuplicateNid, Constants.DUPLICATED_NID);
						resultsEvaluated++;
						continue;
					} else if (hasNoResult(vl)) {
						notProcessedNoResult = vl.getRequestId();
						updatelabResultStatus(notProcessedNoResult, Constants.VIRAL_LOAD_NO_RESULT);
						resultsEvaluated++;
						continue;
					} else if (!GenericUtil.isNumeric(vl.getFinalViralLoadResult().trim())
							&& !(vl.getFinalViralLoadResult().contains(Constants.LESS_THAN))
							&& !(vl.getFinalViralLoadResult().contains(Constants.MORE_THAN))
							&& !(vl.getFinalViralLoadResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL))) {
						notProcessedFlaggedReview = vl.getRequestId();
						updatelabResultStatus(notProcessedFlaggedReview, Constants.FLAGGED_FOR_REVIEW);
						resultsEvaluated++;
						continue;
					} else if (vl.getFinalViralLoadResult().contains(Constants.MORE_THAN)) {
						String trim = vl.getFinalViralLoadResult().trim();
						String maybeNumeric = trim.substring(trim.indexOf(Constants.MORE_THAN) + 1).trim();
						if (!GenericUtil.isNumeric(maybeNumeric)) {
							notProcessedFlaggedReview = vl.getRequestId();
							updatelabResultStatus(notProcessedFlaggedReview, Constants.FLAGGED_FOR_REVIEW);
							resultsEvaluated++;
							continue;
						}
					}

					processed = disa.getRequestId();
					lstPatient = disaService.getPatientByPatientId(patientIds.get(0));
					encounter.setPatient(lstPatient.get(0));
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

				createFsrObs(vl, encounter, dateWithLeadingZeros);

				// log the fsr in openmrs
				FsrLog fsrLog = new FsrLog();
				fsrLog.setPatientId(encounter.getPatient().getPatientId());
				fsrLog.setEncounterId(encounter.getEncounterId());
				fsrLog.setPatientIdentifier(disa.getNid());
				fsrLog.setRequestId(disa.getRequestId());
				fsrLog.setCreator(Context.getAuthenticatedUser().getId());
				fsrLog.setDateCreated(new Date());
				disaService.saveFsrLog(fsrLog);
				eLabsCreated++;

				updateProcessed();
				resultsEvaluated++;
			} else {
				// update with not processing cause DUPLICATE_REQUEST_ID
				notProcessed = disa.getRequestId();
				updatelabResultStatus(notProcessed, Constants.DUPLICATED_REQUEST_ID);
				resultsEvaluated++;
			}
		}

		logger.debug("Sync summary: Initially pending {}, Successfuly evaluated {}, E-Labs created {}",
				jsonViralLoad.size(), resultsEvaluated, eLabsCreated);

		lstPatient.clear();
		jsonViralLoad.clear();
		patientIds.clear();

		logger.info("Syncing ended...");
	}

	private void createFsrObs(HIVVLLabResult labResult, Encounter encounter, Date dateWithLeadingZeros) throws ParseException {
		Obs obs_23835 = new Obs();
		obs_23835.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23835.setObsDatetime(dateWithLeadingZeros);
		obs_23835.setConcept(conceptService.getConceptByUuid(Constants.LAB_NUMBER));
		obs_23835.setLocation(locationBySismaCode);
		obs_23835.setEncounter(encounter);
		if (StringUtils.isNotEmpty(labResult.getHealthFacilityLabCode())) {// RequestingFacilityCode
			obs_23835.setValueText(labResult.getHealthFacilityLabCode());
			obsService.saveObs(obs_23835, "");
		}

		Obs obs_23883 = new Obs();
		obs_23883.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23883.setObsDatetime(dateWithLeadingZeros);
		obs_23883.setConcept(conceptService.getConceptByUuid(Constants.PICKING_LOCATION));
		obs_23883.setLocation(locationBySismaCode);
		obs_23883.setEncounter(encounter);
		if (StringUtils.isNotEmpty(labResult.getRequestingFacilityName())) {// RequestingFacilityName
			obs_23883.setValueText(labResult.getRequestingFacilityName());
			obsService.saveObs(obs_23883, "");
		}

		Obs obs_23836 = new Obs();
		obs_23836.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23836.setObsDatetime(dateWithLeadingZeros);
		obs_23836.setConcept(conceptService.getConceptByUuid(Constants.ENCOUNTER_SERVICE));
		obs_23836.setLocation(locationBySismaCode);
		obs_23836.setEncounter(encounter);
		if (!(labResult.getEncounter() == null)
				&& StringUtils.isNotEmpty(GenericUtil.wardSelection(labResult.getEncounter().trim()))) {// WARD
			obs_23836.setValueCoded(conceptService
					.getConceptByName(GenericUtil.wardSelection(labResult.getEncounter().trim())));
			obsService.saveObs(obs_23836, "");
		}

		Obs obs_1982 = new Obs();
		obs_1982.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_1982.setObsDatetime(dateWithLeadingZeros);
		obs_1982.setConcept(conceptService.getConceptByUuid(Constants.PREGNANT));
		obs_1982.setLocation(locationBySismaCode);
		obs_1982.setEncounter(encounter);
		if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.YES)
				|| labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// Pregnant
			obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.GESTATION));
			obsService.saveObs(obs_1982, "");
		} else if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.NO)
				|| StringUtils.stripAccents(labResult.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
			obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
			obsService.saveObs(obs_1982, "");
		} else {
			obs_1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
			obsService.saveObs(obs_1982, "");
		}

		Obs obs_6332 = new Obs();
		obs_6332.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_6332.setObsDatetime(dateWithLeadingZeros);
		obs_6332.setConcept(conceptService.getConceptByUuid(Constants.LACTATION));
		obs_6332.setLocation(locationBySismaCode);
		obs_6332.setEncounter(encounter);
		if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
				|| labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// BreastFeeding
			obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_YES));
			obsService.saveObs(obs_6332, "");
		} else if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)
				|| StringUtils.stripAccents(labResult.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
			obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
			obsService.saveObs(obs_6332, "");
		} else {
			obs_6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
			obsService.saveObs(obs_6332, "");
		}

		Obs obs_23818 = new Obs();
		obs_23818.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23818.setObsDatetime(dateWithLeadingZeros);
		obs_23818.setConcept(conceptService.getConceptByUuid(Constants.REASON_FOR_TEST));
		obs_23818.setLocation(locationBySismaCode);
		obs_23818.setEncounter(encounter);
		if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {// ReasonForTest
			obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.ROUTINE_VIRAL_LOAD));
			obsService.saveObs(obs_23818, "");
		} else if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) {
			obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.REGIMEN_FAILURE));
			obsService.saveObs(obs_23818, "");
		} else if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.RAB)) {
			obs_23818.setValueCoded(
					conceptService.getConceptByUuid(Constants.REPEAT_AFTER_BREASTFEEDING));
			obsService.saveObs(obs_23818, "");
		} else {
			obs_23818.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
			obsService.saveObs(obs_23818, "");
		}

		Obs obs_23821 = new Obs();
		obs_23821.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23821.setObsDatetime(dateWithLeadingZeros);
		obs_23821.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
		obs_23821.setLocation(locationBySismaCode);
		obs_23821.setEncounter(encounter);
		if (labResult.getHarvestDate() != null) {// SpecimenDatetime
			obs_23821.setValueDate(DateUtil.parseAtMidnight(labResult.getHarvestDate()));
			obsService.saveObs(obs_23821, "");
		}

		Obs obs_23824 = new Obs();
		obs_23824.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23824.setObsDatetime(dateWithLeadingZeros);
		obs_23824.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD));
		obs_23824.setLocation(locationBySismaCode);
		obs_23824.setEncounter(encounter);
		if (!(labResult.getHarvestType() == null)) {
			if (GenericUtil.removeAccents(labResult.getHarvestType().trim()).equalsIgnoreCase(Constants.PV)) {// TypeOfSampleCollection
				obs_23824
						.setValueCoded(conceptService.getConceptByUuid(Constants.VENOUS_PUNCTURE));
				obsService.saveObs(obs_23824, "");
			} else if (GenericUtil.removeAccents(labResult.getHarvestType().trim()).equalsIgnoreCase(Constants.PD)) {
				obs_23824.setValueCoded(
						conceptService.getConceptByUuid(Constants.DIGITAL_PUNCTURE));
				obsService.saveObs(obs_23824, "");
			}
		}

		Obs obs_23826 = new Obs();
		obs_23826.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23826.setObsDatetime(dateWithLeadingZeros);
		obs_23826.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
		obs_23826.setLocation(locationBySismaCode);
		obs_23826.setEncounter(encounter);
		if (labResult.getDateOfSampleReceive() != null) {// ReceivedDateTime
			obs_23826.setValueDate(DateUtil.parseAtMidnight(labResult.getDateOfSampleReceive()));
			obsService.saveObs(obs_23826, "");
		}

		Obs obs_23833 = new Obs();
		obs_23833.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23833.setObsDatetime(dateWithLeadingZeros);
		obs_23833.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
		obs_23833.setLocation(locationBySismaCode);
		obs_23833.setEncounter(encounter);
		if (labResult.getProcessingDate() != null) {// AnalysisDateTime
			obs_23833.setValueDate(DateUtil.parseAtMidnight(labResult.getProcessingDate()));
			obsService.saveObs(obs_23833, "");
		}

		// LastViralLoadResult & LastViralLoadDate
		Obs obs_165314 = new Obs();
		obs_165314.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_165314.setObsDatetime(dateWithLeadingZeros);
		obs_165314.setConcept(conceptService.getConceptByUuid(Constants.LAST_VIRALLOAD_RESULT));
		obs_165314.setLocation(locationBySismaCode);
		obs_165314.setEncounter(encounter);
		obs_165314.setValueText(labResult.getLastViralLoadResult());
		if (DateUtil.isValidDate(labResult.getLastViralLoadDate())) {
			obs_165314.setObsDatetime((DateUtil.stringToDate(labResult.getLastViralLoadDate())));
			obsService.saveObs(obs_165314, "");
		}

		// PrimeiraLinha & SegundaLinha
		Obs obs_21151 = new Obs();
		obs_21151.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_21151.setObsDatetime(dateWithLeadingZeros);
		obs_21151.setConcept(conceptService.getConceptByUuid(Constants.LINHA_TERAPEUTICA));
		obs_21151.setLocation(locationBySismaCode);
		obs_21151.setEncounter(encounter);
		if (labResult.getPrimeiraLinha().equalsIgnoreCase(Constants.SIM)) {
			obs_21151.setValueCoded(conceptService.getConceptByUuid(Constants.PRIMEIRA_LINHA));
			obsService.saveObs(obs_21151, "");
		} else if (labResult.getSegundaLinha().equalsIgnoreCase(Constants.SIM)) {
			obs_21151.setValueCoded(conceptService.getConceptByUuid(Constants.SEGUNDA_LINHA));
			obsService.saveObs(obs_21151, "");
		}

		// ARTRegimen
		Obs obs_165315 = new Obs();
		obs_165315.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_165315.setObsDatetime(dateWithLeadingZeros);
		obs_165315.setConcept(conceptService.getConceptByUuid(Constants.ART_REGIMEN));
		obs_165315.setLocation(locationBySismaCode);
		obs_165315.setEncounter(encounter);
		if (StringUtils.isNotEmpty(labResult.getArtRegimen())) {
			obs_165315.setValueText(labResult.getArtRegimen());
			obsService.saveObs(obs_165315, "");
		}

		// DataDeInicioDoTARV
		Obs obs_1190 = new Obs();
		obs_1190.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_1190.setObsDatetime(dateWithLeadingZeros);
		obs_1190.setConcept(conceptService.getConceptByUuid(Constants.ART_START_DATE));
		obs_1190.setLocation(locationBySismaCode);
		obs_1190.setEncounter(encounter);
		if (DateUtil.isValidDate(labResult.getDataDeInicioDoTARV())) {
			obs_1190.setValueDatetime(DateUtil.stringToDate(labResult.getDataDeInicioDoTARV()));
			obsService.saveObs(obs_1190, "");
		}

		// SpecimenDatetime
		Obs obs_23840 = new Obs();
		obs_23840.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23840.setObsDatetime(dateWithLeadingZeros);
		obs_23840.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_REQUEST_DATE));
		obs_23840.setLocation(locationBySismaCode);
		obs_23840.setEncounter(encounter);
		if (labResult.getHarvestDate() != null) {
			obs_23840.setValueDate(DateUtil.parseAtMidnight(labResult.getHarvestDate()));
			obsService.saveObs(obs_23840, "");
		}

		// LIMSSpecimenSourceCode
		Obs obs_23832 = new Obs();
		obs_23832.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23832.setObsDatetime(dateWithLeadingZeros);
		obs_23832.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_TYPE));
		obs_23832.setLocation(locationBySismaCode);
		obs_23832.setEncounter(encounter);
		if (!(labResult.getSampleType() == null)
				&& labResult.getSampleType().trim().equalsIgnoreCase(Constants.DRYBLOODSPOT)) {
			obs_23832.setValueCoded(conceptService.getConceptByUuid(Constants.DRY_BLOOD_SPOT));
			obsService.saveObs(obs_23832, "");
		} else if (!(labResult.getSampleType() == null)
				&& labResult.getSampleType().trim().equalsIgnoreCase(Constants.PLASMA)) {
			obs_23832.setValueCoded(conceptService.getConceptByUuid(Constants.PLASMA_));
			obsService.saveObs(obs_23832, "");
		}

		// Using FinalViralLoadResult column only
		// field: Cópias/ml
		if (GenericUtil.isNumeric(labResult.getFinalViralLoadResult().trim())) {
			Obs obs_856 = new Obs();
			obs_856.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
			obs_856.setObsDatetime(dateWithLeadingZeros);
			obs_856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
			obs_856.setLocation(locationBySismaCode);
			obs_856.setEncounter(encounter);
			obs_856.setValueNumeric(Double.valueOf(labResult.getFinalViralLoadResult().trim()));
			obsService.saveObs(obs_856, "");
		} else

		// field: dropbox with answer label Indectetável
		if (labResult.getFinalViralLoadResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL)) {
			Obs obs_1305 = new Obs();
			obs_1305.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
			obs_1305.setObsDatetime(dateWithLeadingZeros);
			obs_1305.setConcept(
					conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
			obs_1305.setValueCoded(
					conceptService.getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD));
			obs_1305.setLocation(locationBySismaCode);
			obs_1305.setEncounter(encounter);
			obsService.saveObs(obs_1305, "");
		} else

		// field: dropbox with answer <
		if (labResult.getFinalViralLoadResult().contains(Constants.LESS_THAN)) {
			Obs obs_1305 = new Obs();
			obs_1305.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
			obs_1305.setObsDatetime(dateWithLeadingZeros);
			obs_1305.setConcept(
					conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
			obs_1305.setValueCoded(conceptService.getConceptByUuid(Constants.LESSTHAN));
			obs_1305.setLocation(locationBySismaCode);
			obs_1305.setEncounter(encounter);
			obs_1305.setComment(labResult.getFinalViralLoadResult()
					.trim()
					.substring(1)
					.replace(Constants.LESS_THAN, "")
					.replace(Constants.COPIES, "")
					.replace(Constants.FORWARD_SLASH, "")
					.replace(Constants.ML, "")
					.trim());
			obsService.saveObs(obs_1305, "");
		} else if (labResult.getFinalViralLoadResult().contains(Constants.MORE_THAN)) {
			Obs obs_856 = new Obs();
			obs_856.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
			obs_856.setObsDatetime(dateWithLeadingZeros);
			obs_856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
			obs_856.setLocation(locationBySismaCode);
			obs_856.setEncounter(encounter);
			String trim = labResult.getFinalViralLoadResult().trim();
			String numericPart = trim.substring(trim.indexOf(Constants.MORE_THAN) + 1);
			obs_856.setValueNumeric(Double.valueOf(numericPart));
			obsService.saveObs(obs_856, "");
		}

		Obs obs_23839 = new Obs();
		obs_23839.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23839.setObsDatetime(dateWithLeadingZeros);
		obs_23839.setConcept(conceptService.getConceptByUuid(Constants.APPROVED_BY));
		obs_23839.setEncounter(encounter);
		obs_23839.setLocation(locationBySismaCode);
		if (!(labResult.getAprovedBy() == null) && StringUtils.isNotEmpty(labResult.getAprovedBy().trim())) {
			obs_23839.setValueText(labResult.getAprovedBy().trim());
			obsService.saveObs(obs_23839, "");
		}

		Obs obs_23841 = new Obs();
		obs_23841.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_23841.setObsDatetime(dateWithLeadingZeros);
		obs_23841.setConcept(conceptService.getConceptByUuid(Constants.LAB_COMMENTS));
		obs_23841.setLocation(locationBySismaCode);
		obs_23841.setEncounter(encounter);
		if (!(labResult.getLabComments() == null) && StringUtils.isNotEmpty(labResult.getLabComments().trim())) {
			obs_23841.setValueText(labResult.getLabComments().trim());
			obsService.saveObs(obs_23841, "");
		}

		Obs obs_22771 = new Obs();
		obs_22771.setPerson(personService.getPersonByUuid(lstPatient.get(0).getUuid()));
		obs_22771.setObsDatetime(dateWithLeadingZeros);
		obs_22771.setConcept(conceptService.getConceptByUuid(Constants.ORDER_ID));
		obs_22771.setLocation(locationBySismaCode);
		obs_22771.setEncounter(encounter);
		if (!(labResult.getRequestId() == null) && StringUtils.isNotEmpty(labResult.getRequestId().trim())) {
			obs_22771.setValueText(labResult.getRequestId().trim());
			obsService.saveObs(obs_22771, "");
		}
	}

	private boolean hasNoResult(HIVVLLabResult vl) {
		return (vl.getFinalViralLoadResult() == null || vl.getFinalViralLoadResult().isEmpty());
	}

	private List<LabResult> getJsonViralLoad() {
		try {
			return rest.getRequestGet(getAllDisaSismaCodes(), getDisaProvince());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private void updatelabResultStatus(String notProcessedNid, String reasonForNotProcessing) throws Exception {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessedNid, reasonForNotProcessing,
					defaultLocationUuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateProcessed() throws Exception {
		try {
			rest.getRequestPutProcessed(Constants.URL_PATH_PROCESSED, processed, defaultLocationUuid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Location getLocationBySismaCode(String sismaCode) {
		String locationAttrType = administrationService
				.getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID).getPropertyValue();
		LocationAttributeType locationAttributeType = locationService
				.getLocationAttributeTypeByUuid(locationAttrType);
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
}
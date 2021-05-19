package org.openmrs.module.disa.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.GenericUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author machabane
 *
 */
public class ViralLoadFormSchedulerTask extends AbstractTask {
	private List<String> processed;
	private List<String> notProcessed;
	private List<String> notProcessedNoResult;
	private RestUtil rest;
	private DisaService disaService;
	private Location locationBySismaCode;

	public ViralLoadFormSchedulerTask() {
		disaService = Context.getService(DisaService.class);
		rest = new RestUtil();
		rest.setURLBase(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
	}

	@Override
	public void execute() {
		Context.openSession();
		createViralLoadForm();
		Context.closeSession();
	}

	private void createViralLoadForm() {

		// iterate the viral load list and create the encounters
		processed = new ArrayList<String>();
		notProcessed = new ArrayList<String>();
		notProcessedNoResult = new ArrayList<String>();
		
		List<Disa> jsonViralLoad = getJsonViralLoad(); 
		System.out.println("There is " + jsonViralLoad.size() + " pending items to be processed");
		
		for (Disa disa : jsonViralLoad) {
			Encounter encounter = new Encounter();

			encounter.setEncounterDatetime(DateUtil.deserialize(disa.getCreatedAt())); 
			List<Patient> patientsByIdentifier = Context.getPatientService()
					.getPatients(null, disa.getNid().trim(), null, Boolean.TRUE);

			if (patientsByIdentifier.isEmpty()) {
				notProcessed.add(disa.getRequestId());
				continue;
			} else {
				if (hasNoResult(disa)) {
					notProcessedNoResult.add(disa.getRequestId());
					continue;
				} else {

					processed.add(disa.getRequestId());
					encounter.setPatient(patientsByIdentifier.get(0));
				}
			}

			locationBySismaCode = getLocationBySismaCode(disa.getHealthFacilityLabCode());
			encounter.setEncounterType(
					Context.getEncounterService().getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE));
			encounter.setLocation(locationBySismaCode);
			encounter.setForm(Context.getFormService().getFormByUuid(Constants.DISA_FORM));

			encounter.setProvider(
					Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID),
					Context.getProviderService().getProviderByUuid(Constants.DISA_PROVIDER));

			Context.getEncounterService().saveEncounter(encounter);

			Obs obs_23835 = new Obs();
			obs_23835.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23835.setObsDatetime(new Date());
			obs_23835.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_NUMBER));
			obs_23835.setLocation(locationBySismaCode);
			obs_23835.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getHealthFacilityLabCode())) {
				obs_23835.setValueText(disa.getHealthFacilityLabCode());
				Context.getObsService().saveObs(obs_23835, "");
			}

			Obs obs_23883 = new Obs();
			obs_23883.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23883.setObsDatetime(new Date());
			obs_23883.setConcept(Context.getConceptService().getConceptByUuid(Constants.PICKING_LOCATION));
			obs_23883.setLocation(locationBySismaCode);
			obs_23883.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getRequestingFacilityName())) {
				obs_23883.setValueText(disa.getRequestingFacilityName());
				Context.getObsService().saveObs(obs_23883, "");
			}

			Obs obs_23836 = new Obs();
			obs_23836.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23836.setObsDatetime(new Date());
			obs_23836.setConcept(Context.getConceptService().getConceptByUuid(Constants.ENCOUNTER_SERVICE));
			obs_23836.setLocation(locationBySismaCode);
			obs_23836.setEncounter(encounter);
			if (StringUtils.isNotEmpty(GenericUtil.wardSelection(disa.getEncounter().trim()))) {
				obs_23836.setValueCoded(Context.getConceptService()
						.getConceptByName(GenericUtil.wardSelection(disa.getEncounter().trim())));
				Context.getObsService().saveObs(obs_23836, "");
			}

			Obs obs_1982 = new Obs();
			obs_1982.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_1982.setObsDatetime(new Date());
			obs_1982.setConcept(Context.getConceptService().getConceptByUuid(Constants.PREGNANT));
			obs_1982.setLocation(locationBySismaCode);
			obs_1982.setEncounter(encounter);
			if (disa.getPregnant().trim().equalsIgnoreCase(Constants.YES)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.GESTATION));
				Context.getObsService().saveObs(obs_1982, "");
			} else if (disa.getPregnant().trim().equalsIgnoreCase(Constants.NO)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NO));
				Context.getObsService().saveObs(obs_1982, "");
			} else if (GenericUtil.removeAccents(disa.getPregnant().trim()).equalsIgnoreCase(Constants.NF)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
				Context.getObsService().saveObs(obs_1982, "");
			}

			Obs obs_6332 = new Obs();
			obs_6332.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_6332.setObsDatetime(new Date());
			obs_6332.setConcept(Context.getConceptService().getConceptByUuid(Constants.LACTATION));
			obs_6332.setLocation(locationBySismaCode);
			obs_6332.setEncounter(encounter);
			if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)) {
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_YES));
				Context.getObsService().saveObs(obs_6332, "");
			} else if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)) {
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NO));
				Context.getObsService().saveObs(obs_6332, "");
			} else if (GenericUtil.removeAccents(disa.getBreastFeeding().trim()).equalsIgnoreCase(Constants.NF)) {
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
				Context.getObsService().saveObs(obs_6332, "");
			}

			Obs obs_23818 = new Obs();
			obs_23818.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23818.setObsDatetime(new Date());
			obs_23818.setConcept(Context.getConceptService().getConceptByUuid(Constants.REASON_FOR_TEST));
			obs_23818.setLocation(locationBySismaCode);
			obs_23818.setEncounter(encounter);
			if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.ROUTINE_VIRAL_LOAD));
				Context.getObsService().saveObs(obs_23818, "");
			} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) {
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.REGIMEN_FAILURE));
				Context.getObsService().saveObs(obs_23818, "");
			} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.RAB)) {
				obs_23818.setValueCoded(
						Context.getConceptService().getConceptByUuid(Constants.REPEAT_AFTER_BREASTFEEDING));
				Context.getObsService().saveObs(obs_23818, "");
			} else if (GenericUtil.removeAccents(disa.getReasonForTest().trim()).equalsIgnoreCase(Constants.NF)) {
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
				Context.getObsService().saveObs(obs_23818, "");
			}

			Obs obs_23821 = new Obs();
			obs_23821.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23821.setObsDatetime(new Date());
			obs_23821.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
			obs_23821.setLocation(locationBySismaCode);
			obs_23821.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getHarvestDate())) {
				obs_23821.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
				Context.getObsService().saveObs(obs_23821, "");
			}

			Obs obs_23824 = new Obs();
			obs_23824.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23824.setObsDatetime(new Date());
			obs_23824.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD));
			obs_23824.setLocation(locationBySismaCode);
			obs_23824.setEncounter(encounter);
			if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PV)) {
				obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.VENOUS_PUNCTURE));
				Context.getObsService().saveObs(obs_23824, "");
			} else if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PD)) {
				obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.DIGITAL_PUNCTURE));
				Context.getObsService().saveObs(obs_23824, "");
			}

			Obs obs_23826 = new Obs();
			obs_23826.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23826.setObsDatetime(new Date());
			obs_23826.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
			obs_23826.setLocation(locationBySismaCode);
			obs_23826.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getDateOfSampleReceive())) {
				obs_23826.setValueDate(DateUtil.deserialize(disa.getDateOfSampleReceive()));
				Context.getObsService().saveObs(obs_23826, "");
			}

			Obs obs_23833 = new Obs();
			obs_23833.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23833.setObsDatetime(new Date());
			obs_23833.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
			obs_23833.setLocation(locationBySismaCode);
			obs_23833.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getProcessingDate())) {
				obs_23833.setValueDate(DateUtil.deserialize(disa.getProcessingDate()));
				Context.getObsService().saveObs(obs_23833, "");
			}

			Obs obs_23832 = new Obs();
			obs_23832.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23832.setObsDatetime(new Date());
			obs_23832.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_TYPE));
			obs_23832.setLocation(locationBySismaCode);
			obs_23832.setEncounter(encounter);
			if (disa.getSampleType().trim().equalsIgnoreCase(Constants.DBS)) {
				obs_23832.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.DRY_BLOOD_SPOT));
				Context.getObsService().saveObs(obs_23832, "");
			}

			Obs obs_856 = new Obs();
			obs_856.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_856.setObsDatetime(new Date());
			obs_856.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
			obs_856.setLocation(locationBySismaCode);
			obs_856.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getHivViralLoadResult())) {

				obs_856.setValueNumeric(Double.valueOf(-20));
				Context.getObsService().saveObs(obs_856, "");

				Obs obs_1306 = new Obs();
				obs_1306.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
				obs_1306.setObsDatetime(new Date());
				obs_1306.setConcept(Context.getConceptService().getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
				obs_1306.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.BEYOND_DETECTABLE_LIMIT));
				obs_1306.setLocation(locationBySismaCode);
				obs_1306.setEncounter(encounter);
				Context.getObsService().saveObs(obs_1306, "");
			} else if (StringUtils.isNotEmpty(disa.getViralLoadResultCopies())
					&& NumberUtils.isNumber(disa.getViralLoadResultCopies().trim())) {
				obs_856.setValueNumeric(Double.valueOf(disa.getViralLoadResultCopies().trim()));
				Context.getObsService().saveObs(obs_856, "");
			} else if (StringUtils.isNotEmpty(disa.getViralLoadResultCopies())
					&& disa.getViralLoadResultCopies().contains("<")) {
				obs_856.setValueNumeric(Double.valueOf(-20));
				Context.getObsService().saveObs(obs_856, "");

				Obs obs_1306 = new Obs();
				obs_1306.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
				obs_1306.setObsDatetime(new Date());
				obs_1306.setConcept(Context.getConceptService().getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
				obs_1306.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.BEYOND_DETECTABLE_LIMIT));
				obs_1306.setLocation(locationBySismaCode);
				obs_1306.setEncounter(encounter);
				Context.getObsService().saveObs(obs_1306, "");
			}

			Obs obs_165243 = new Obs();
			obs_165243.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_165243.setObsDatetime(new Date());
			obs_165243.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_LOG));
			obs_165243.setLocation(locationBySismaCode);
			obs_165243.setEncounter(encounter);
			obs_165243.setValueNumeric(disa.getViralLoadResultLog() == null ? Double.valueOf(0.0)
					: Double.valueOf(disa.getViralLoadResultLog().toString().replace("<", "").trim()));
			Context.getObsService().saveObs(obs_165243, "");

			Obs obs_23839 = new Obs();
			obs_23839.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23839.setObsDatetime(new Date());
			obs_23839.setConcept(Context.getConceptService().getConceptByUuid(Constants.APPROVED_BY));
			obs_23839.setEncounter(encounter);
			obs_23839.setLocation(locationBySismaCode);
			if (StringUtils.isNotEmpty(disa.getAprovedBy().trim())) {
				obs_23839.setValueText(disa.getAprovedBy().trim());
				Context.getObsService().saveObs(obs_23839, "");
			}

			Obs obs_23841 = new Obs();
			obs_23841.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23841.setObsDatetime(new Date());
			obs_23841.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_COMMENTS));
			obs_23841.setLocation(locationBySismaCode);
			obs_23841.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getLabComments().trim())) {
				obs_23841.setValueText(disa.getLabComments().trim());
				Context.getObsService().saveObs(obs_23841, "");
			}

			Obs obs_22771 = new Obs();
			obs_22771.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_22771.setObsDatetime(new Date());
			obs_22771.setConcept(Context.getConceptService().getConceptByUuid(Constants.ORDER_ID));
			obs_22771.setLocation(locationBySismaCode);
			obs_22771.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getRequestId().trim())) {
				obs_22771.setValueText(disa.getRequestId().trim());
				Context.getObsService().saveObs(obs_22771, "");
			}
		}

		if (processed.size() > 0) updateProcessed();
		if (notProcessed.size() > 0) updateNotProcessed();
		if (notProcessedNoResult.size() > 0) updateNotProcessedNoResult();
	}

	private boolean hasNoResult(Disa disa) {
		return (disa.getViralLoadResultCopies() == null || disa.getViralLoadResultCopies().isEmpty())
				&& (disa.getViralLoadResultLog() == null || disa.getViralLoadResultLog().isEmpty())
				&& (disa.getHivViralLoadResult() == null || disa.getHivViralLoadResult().isEmpty());
	}

	private List<Disa> getJsonViralLoad() {
		String jsonViralLoadInfo = null;

		try {
			jsonViralLoadInfo = rest.getRequestGet(getAllDisaSismaCodes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {
		}.getType());
	}

	private void updateProcessed() {
		try {
			rest.getRequestPutProcessed(Constants.URL_PATH_PROCESSED, processed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessed() {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessed, "nid");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNotProcessedNoResult() {
		try {
			rest.getRequestPutNotProcessed(Constants.URL_PATH_NOT_PROCESSED, notProcessedNoResult, "result");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Location getLocationBySismaCode(String sismaCode) {
		LocationAttributeType locationAttributeType = Context.getLocationService()
				.getLocationAttributeTypeByUuid(Constants.LOCATION_ATTRIBUTE_TYPE_UUID);
		Map<LocationAttributeType, Object> hashMap = new HashMap<LocationAttributeType, Object>();
		hashMap.put(locationAttributeType, sismaCode);
		List<Location> locations = Context.getLocationService().getLocations(null, null, hashMap, false, null, null);

		return locations.get(0);
	}

	private List<String> getAllDisaSismaCodes() {
		List<String> valueReferences = new ArrayList<String>();
		List<LocationAttribute> allLocationAttribute = disaService.getAllLocationAttribute();
		for (LocationAttribute locationAttribute : allLocationAttribute) {
			valueReferences.add(locationAttribute.getValueReference());
		}
		return valueReferences;
	}
}
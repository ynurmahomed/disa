package org.openmrs.module.disa.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author machabane
 *
 */
public class ViralLoadFormSchedulerTask extends AbstractTask {
	private static final Logger log = LoggerFactory.getLogger(ViralLoadFormSchedulerTask.class);
	private List<String> processed;
	private List<String> notProcessed;
	private RestUtil rest;
	
	public ViralLoadFormSchedulerTask() {
		log.info("ViralLoadFormSchedulerTask method called");
		rest = new RestUtil();
		rest.setURLBase(Context.getAdministrationService().getGlobalPropertyObject("disa.api.url").getPropertyValue());
		rest.setUsername(Context.getAdministrationService().getGlobalPropertyObject("disa.api.username").getPropertyValue());
		rest.setPassword(Context.getAdministrationService().getGlobalPropertyObject("disa.api.password").getPropertyValue());
	}
	
	@Override
	public void execute() { 
		Context.openSession();
			createViralLoadForm();
		Context.closeSession();
	}

	@SuppressWarnings("deprecation")
	private void createViralLoadForm() {
		//iterate the viral load list and create the encounters
		processed = new ArrayList<String>();
		notProcessed = new ArrayList<String>();
		for (Disa disa : getJsonViralLoad()) {
			Encounter encounter = new Encounter();
			
			encounter.setEncounterDatetime(new Date());
			List<Patient> patientsByIdentifier = Context.getPatientService().getPatientsByIdentifier(disa.getNid().trim(), Boolean.FALSE);
			
			if (patientsByIdentifier.equals(null)){ 
				notProcessed.add(disa.getNid());
				continue;
			} else {
				processed.add(disa.getNid());
				encounter.setPatient(patientsByIdentifier.get(0));
			}
			
			encounter.setEncounterType(Context.getEncounterService().
					getEncounterTypeByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.encounterType").getPropertyValue()));
			
			encounter.setLocation(Context.getLocationService().
					getLocationByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.location").getPropertyValue()));
			
			encounter.setForm(Context.getFormService().
					getFormByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.form").getPropertyValue()));
			
			encounter.setProvider(Context.getEncounterService().getEncounterRoleByUuid(
					EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), Context.getProviderService().
					getProviderByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.provider").getPropertyValue())); 
						
			Context.getEncounterService().saveEncounter(encounter);
			
			//observations
			Obs obs_23835 = new Obs();
			obs_23835.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23835.setObsDatetime(new Date());
			obs_23835.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_NUMBER));
			obs_23835.setValueText(disa.getHealthFacilityLabCode());
			obs_23835.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23835,"");
			
			Obs obs_23836 = new Obs();
			obs_23836.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23836.setObsDatetime(new Date());
			obs_23836.setConcept(Context.getConceptService().getConceptByUuid(Constants.ENCOUNTER_SERVICE));
			obs_23836.setValueCoded(Context.getConceptService().getConceptByName(disa.getEncounter().trim()));
			obs_23836.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23836,"");
			
			//1982
			Obs obs_1982 = new Obs();
			obs_1982.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_1982.setObsDatetime(new Date());
			obs_1982.setConcept(Context.getConceptService().getConceptByUuid(Constants.PREGNANT));
			obs_1982.setEncounter(encounter);
			if (disa.getPregnant().trim().equalsIgnoreCase(Constants.YES)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.GESTATION));
				Context.getObsService().saveObs(obs_1982,"");
			} else if (disa.getPregnant().trim().equalsIgnoreCase(Constants.NO)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NO));
				Context.getObsService().saveObs(obs_1982,"");
			} else if (disa.getPregnant().trim().equalsIgnoreCase(Constants.NAO_PREENCHIDO)) {
				obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.NOT_FILLED));
				Context.getObsService().saveObs(obs_1982,"");
			}
			
			//6332
			Obs obs_6332 = new Obs();
			obs_6332.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_6332.setObsDatetime(new Date());
			obs_6332.setConcept(Context.getConceptService().getConceptByUuid(Constants.LACTATION));
			obs_6332.setEncounter(encounter);
			if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)) {
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid("e1d81b62-1d5f-11e0-b929-000c29ad1d07"));
				Context.getObsService().saveObs(obs_6332,"");
			} else if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)) {
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid("e1d81c70-1d5f-11e0-b929-000c29ad1d07"));
				Context.getObsService().saveObs(obs_6332,"");
			} else if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.NAO_PREENCHIDO)) { 
				obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid("e1d81d7e-1d5f-11e0-b929-000c29ad1d07"));
				Context.getObsService().saveObs(obs_6332,"");
			}
			
			Obs obs_23818 = new Obs();
			obs_23818.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23818.setObsDatetime(new Date());
			obs_23818.setConcept(Context.getConceptService().getConceptByUuid(Constants.REASON_FOR_TEST));
			obs_23818.setEncounter(encounter);
			if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid("971cf484-2751-40ce-9f89-d23f544d06e2"));
				Context.getObsService().saveObs(obs_23818,"");
			} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) { 
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid("e1d616b4-1d5f-11e0-b929-000c29ad1d07"));  
				Context.getObsService().saveObs(obs_23818,"");
			} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.REPEAT_AFTER_BREASTFEEDING)) { 
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid("6f60eaa1-886d-4891-9d3d-7ade75d67606"));  
				Context.getObsService().saveObs(obs_23818,"");
			} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.NAO_PREENCHIDO)) { 
				obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid("e1d81d7e-1d5f-11e0-b929-000c29ad1d07"));  
				Context.getObsService().saveObs(obs_23818,"");
			}
			
			//23819 - Pending
			
			Obs obs_23821 = new Obs();
			obs_23821.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23821.setObsDatetime(new Date());
			obs_23821.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
			obs_23821.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
			obs_23821.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23821,"");
			
			Obs obs_23824 = new Obs();
			obs_23824.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23824.setObsDatetime(new Date());
			obs_23824.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD)); 
			obs_23824.setEncounter(encounter);
			if (disa.getHarvestType().trim().equalsIgnoreCase(Constants.PUNCAO_VENOSA)) { 
				obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid("3098bd78-a1bb-455c-a724-9c114072b34e"));
				Context.getObsService().saveObs(obs_23824,"");
			} else if (disa.getHarvestType().trim().equalsIgnoreCase(Constants.PUNCAO_DIGITAL)) { 
				obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid("9aa935ec-be0f-4038-a1ca-23927bfde672"));
				Context.getObsService().saveObs(obs_23824,"");
			}

			Obs obs_23826 = new Obs();
			obs_23826.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23826.setObsDatetime(new Date());
			obs_23826.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
			obs_23826.setValueDate(DateUtil.deserialize(disa.getDateOfSampleReceive()));
			obs_23826.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23826,"");
			
			Obs obs_23833 = new Obs();
			obs_23833.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23833.setObsDatetime(new Date());
			obs_23833.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE)); 
			Date processingDate  = DateUtil.deserialize(disa.getProcessingDate());   
			obs_23833.setValueDate(processingDate);
			obs_23833.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23833,"");
			
			Obs obs_23832 = new Obs();
			obs_23832.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23832.setObsDatetime(new Date());
			obs_23832.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_TYPE));
			obs_23832.setEncounter(encounter);
			if (disa.getSampleType().trim().equalsIgnoreCase(Constants.DRY_BLOOD_SPOT)) { 
				obs_23832.setValueCoded(Context.getConceptService().getConceptByUuid("7c288beb-548c-4440-8f12-4f62cd45305a"));
				Context.getObsService().saveObs(obs_23832,"");
			}
						
			Obs obs_856 = new Obs();
			obs_856.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_856.setObsDatetime(new Date());
			obs_856.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_COPIES)); 
			obs_856.setEncounter(encounter);
			if (StringUtils.isNotEmpty(disa.getHivViralLoadResult())) { 
				
				obs_856.setValueNumeric(Double.valueOf(-20));
				Context.getObsService().saveObs(obs_856,"");
				
				Obs obs_1306 = new Obs();
				obs_1306.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
				obs_1306.setObsDatetime(new Date());
				obs_1306.setConcept(Context.getConceptService().getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
				obs_1306.setValueCoded(Context.getConceptService().getConceptByUuid("e1da2812-1d5f-11e0-b929-000c29ad1d07"));
				obs_1306.setEncounter(encounter);
				Context.getObsService().saveObs(obs_1306,"");
			} else if (NumberUtils.isNumber(disa.getViralLoadResultCopies().trim())) {
				obs_856.setValueNumeric(Double.valueOf(disa.getViralLoadResultCopies().trim()));
				Context.getObsService().saveObs(obs_856,"");
			}
			
			Obs obs_1518 = new Obs();
			obs_1518.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_1518.setObsDatetime(new Date());
			obs_1518.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_LOG));
			obs_1518.setValueNumeric(disa.getViralLoadResultLog()==null ? Double.valueOf(0.0) : Double.valueOf(disa.getViralLoadResultLog()));  
			obs_1518.setEncounter(encounter);
			Context.getObsService().saveObs(obs_1518,"");
			
			//1305 - should we collect Suppressed results?  
			
			//viral load result date
			/*Obs obs13 = new Obs();
			obs13.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs13.setObsDatetime(new Date());
			obs13.setConcept(Context.getConceptService().getConceptByUuid(""));
			Date viralLoadResultDate = DateUtil.deserialize(disa.getViralLoadResultDate()); 
			obs13.setValueDate(viralLoadResultDate);
			obs13.setEncounter(encounter);
			Context.getObsService().saveObs(obs13,"");*/
			
			Obs obs_23839 = new Obs();
			obs_23839.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23839.setObsDatetime(new Date());
			obs_23839.setConcept(Context.getConceptService().getConceptByUuid(Constants.APPROVED_BY));
			obs_23839.setValueText(disa.getAprovedBy().trim());
			obs_23839.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23839,"");
			
			Obs obs_23841 = new Obs();
			obs_23841.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23841.setObsDatetime(new Date());
			obs_23841.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_COMMENTS)); 
			obs_23841.setValueText(disa.getLabComments().trim());
			obs_23841.setEncounter(encounter);
			Context.getObsService().saveObs(obs_23841,"");
		}
		
		//update status column
		/*if(processed.size()>0) updateStatusColumn();
		if(notProcessed.size()>0) updateStatusColumn();*/
	}
	
	private List<Disa> getJsonViralLoad() {
		String locationTest = "CHOKW1"; 
		String jsonViralLoadInfo = null; 
		
		try {
			jsonViralLoadInfo = rest.getRequestGet("?locationCodes="+locationTest);
		} catch (Exception e) {e.printStackTrace();}
		
		return new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>(){}.getType()); 
	}
	
	/*private void updateStatusColumn(){
		rest.getRequestPost("", "?processedNids="+processed); 
	}*/
}
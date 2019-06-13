package org.openmrs.module.disa.scheduler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ViralLoadFormSchedulerTask extends AbstractTask {
	
	private static final Logger log = LoggerFactory.getLogger(ViralLoadFormSchedulerTask.class);
	
	private List<Disa> disaList;
	
	public ViralLoadFormSchedulerTask() {
		log.info("ViralLoadFormSchedulerTask method called");
	}
	
	@Override
	public void execute() {
		log.info("execute nethod called");
		
		Context.openSession();
			getJsonViralLoad();
			createViralLoadForm();
		Context.closeSession();
	}

	@SuppressWarnings("deprecation")
	private void createViralLoadForm() {
		log.info("createViralLoadForm called...");
		
		//iterate the viral load list and create the encounters
		for (Disa disa : disaList) {
			Encounter encounter = new Encounter();
			//encounter date
			encounter.setEncounterDatetime(new Date());
			List<Patient> patientsByIdentifier = Context.getPatientService().getPatientsByIdentifier(disa.getNid().trim(), Boolean.FALSE);
			encounter.setPatient(patientsByIdentifier.get(0));
			//encounterType
			encounter.setEncounterType(Context.getEncounterService().
					getEncounterTypeByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.encounterType").getPropertyValue()));
			//location
			encounter.setLocation(Context.getLocationService().
					getLocationByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.location").getPropertyValue()));
			//form
			encounter.setForm(Context.getFormService().
					getFormByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.form").getPropertyValue()));
			//provider			
			encounter.setProvider(Context.getEncounterService().getEncounterRoleByUuid(
					EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), Context.getProviderService().
					getProviderByUuid(Context.getAdministrationService().getGlobalPropertyObject("disa.provider").getPropertyValue())); 
						
			Context.getEncounterService().saveEncounter(encounter);
			
			//observations
			Obs obs = new Obs();
			obs.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs.setObsDatetime(new Date());
			
			//concept 23835
			obs.setConcept(Context.getConceptService().getConceptByUuid("e173835b-135c-4fab-9b5e-b255565980e5"));
			obs.setValueText("2222");
			obs.setEncounter(encounter);
			Context.getObsService().saveObs(obs,"");
			
			//concept 23836
			Obs obs1 = new Obs();
			obs1.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs1.setObsDatetime(new Date());
			obs1.setConcept(Context.getConceptService().getConceptByUuid("f448b038-f0af-4a0f-9a48-635e1838d22d"));
			obs1.setValueCoded(Context.getConceptService().getConceptByName(disa.getEncounter().trim()));
			obs1.setEncounter(encounter);
			Context.getObsService().saveObs(obs1,"");
			
			//1982
			Obs obs3 = new Obs();
			obs3.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs3.setObsDatetime(new Date());
			obs3.setConcept(Context.getConceptService().getConceptByUuid("e1e056a6-1d5f-11e0-b929-000c29ad1d07"));
			String pregnant =  disa.getPregnant().trim().equalsIgnoreCase("YES") ? "e1cdd58a-1d5f-11e0-b929-000c29ad1d07" : "e1d81c70-1d5f-11e0-b929-000c29ad1d07";
			obs3.setValueCoded(Context.getConceptService().getConceptByUuid(pregnant));
			obs3.setEncounter(encounter);
			Context.getObsService().saveObs(obs3,"");
			
			//6332
			Obs obs4 = new Obs();
			obs4.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs4.setObsDatetime(new Date());
			obs4.setConcept(Context.getConceptService().getConceptByUuid("bc4fe755-fc8f-49b8-9956-baf2477e8313"));
			String breastFeeding =  disa.getBreastFeeding().trim().equalsIgnoreCase("YES") ? "e1d81b62-1d5f-11e0-b929-000c29ad1d07" : "e1d81c70-1d5f-11e0-b929-000c29ad1d07";
			obs4.setValueCoded(Context.getConceptService().getConceptByUuid(breastFeeding));
			obs4.setEncounter(encounter);
			Context.getObsService().saveObs(obs4,"");
			
			//23818
			Obs obs5 = new Obs();
			obs5.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs5.setObsDatetime(new Date());
			obs5.setConcept(Context.getConceptService().getConceptByUuid("a97ab290-5b66-4755-bd49-d82f944cd093"));
			String reasonForTest =  disa.getReasonForTest().trim().equalsIgnoreCase("Routine") ? "971cf484-2751-40ce-9f89-d23f544d06e2" : "e1d616b4-1d5f-11e0-b929-000c29ad1d07";
			obs5.setValueCoded(Context.getConceptService().getConceptByUuid(reasonForTest));
			obs5.setEncounter(encounter);
			Context.getObsService().saveObs(obs5,"");
			
			//23819 - Pending
			
			//23821
			Obs obs6 = new Obs();
			obs6.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs6.setObsDatetime(new Date());
			obs6.setConcept(Context.getConceptService().getConceptByUuid("f85e3f84-a255-412a-aa43-40174f69c305"));
			Date specimenDate = DateUtil.deserialize(disa.getHarvestDate());
			obs6.setValueDate(specimenDate);
			obs6.setEncounter(encounter);
			Context.getObsService().saveObs(obs6,"");
			
			//23824
			Obs obs7 = new Obs();
			obs7.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs7.setObsDatetime(new Date());
			obs7.setConcept(Context.getConceptService().getConceptByUuid("b9c83dd2-0c27-4e01-9c70-f1e1125927be"));
			String specimenType = disa.getHarvestType().trim().equalsIgnoreCase("Puncao venosa") ? "3098bd78-a1bb-455c-a724-9c114072b34e" : "";
			obs7.setValueCoded(Context.getConceptService().getConceptByUuid(specimenType));
			obs7.setEncounter(encounter);
			Context.getObsService().saveObs(obs7,"");

			//23826
			Obs obs8 = new Obs();
			obs8.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs8.setObsDatetime(new Date());
			obs8.setConcept(Context.getConceptService().getConceptByUuid("bb753c4b-41f4-411c-9b3e-9b21fc6ce0e4"));
			Date receivedDateTime  = DateUtil.deserialize(disa.getDateOfSampleReceive());  
			obs8.setValueDate(receivedDateTime);
			obs8.setEncounter(encounter);
			Context.getObsService().saveObs(obs8,"");
			
			//23830 - Reason for refusing sample processing
			
			//23827 - Sample processed?
			
			//23833
			Obs obs9 = new Obs();
			obs9.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs9.setObsDatetime(new Date());
			obs9.setConcept(Context.getConceptService().getConceptByUuid("502337d4-94a1-4b0a-a728-567340fa3e79"));
			Date processingDate  = DateUtil.deserialize(disa.getProcessingDate());   
			obs9.setValueDate(processingDate);
			obs9.setEncounter(encounter);
			Context.getObsService().saveObs(obs9,"");
			
			//23832
			Obs obs10 = new Obs();
			obs10.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs10.setObsDatetime(new Date());
			obs10.setConcept(Context.getConceptService().getConceptByUuid("47ee9ffd-8103-4788-ab23-e185c144cf1e"));
			String sampleType =  disa.getSampleType().trim().equalsIgnoreCase("Dry Blood Spot") ? "7c288beb-548c-4440-8f12-4f62cd45305a" : "";
			obs10.setValueCoded(Context.getConceptService().getConceptByUuid(sampleType));
			obs10.setEncounter(encounter);
			Context.getObsService().saveObs(obs10,"");
			
			//856
			Obs obs11 = new Obs();
			obs11.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs11.setObsDatetime(new Date());
			obs11.setConcept(Context.getConceptService().getConceptByUuid("e1d6247e-1d5f-11e0-b929-000c29ad1d07"));
			obs11.setValueNumeric(Double.valueOf(disa.getViralLoadResultCopies().trim()));
			obs11.setEncounter(encounter);
			Context.getObsService().saveObs(obs11,"");
			
			//1518
			Obs obs12 = new Obs();
			obs12.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs12.setObsDatetime(new Date());
			obs12.setConcept(Context.getConceptService().getConceptByUuid("e1dc4bf6-1d5f-11e0-b929-000c29ad1d07"));
			obs12.setValueNumeric(Double.valueOf(disa.getViralLoadResultLog().trim()));
			obs12.setEncounter(encounter);
			Context.getObsService().saveObs(obs12,"");
			
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
			
			//23839
			Obs obs14 = new Obs();
			obs14.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs14.setObsDatetime(new Date());
			obs14.setConcept(Context.getConceptService().getConceptByUuid("2177105d-c57b-446d-9066-e35a44c5b873"));
			obs14.setValueText(disa.getAprovedBy().trim());
			obs14.setEncounter(encounter);
			Context.getObsService().saveObs(obs14,"");
			
			//23841
			Obs obs15 = new Obs();
			obs15.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs15.setObsDatetime(new Date());
			obs15.setConcept(Context.getConceptService().getConceptByUuid("246b0c6b-66f2-4d0d-988b-a0b867693fe6"));
			obs15.setValueText(disa.getLabComments().trim());
			obs15.setEncounter(encounter);
			Context.getObsService().saveObs(obs15,"");
		}
	}
	
	private List<Disa> getJsonViralLoad() {
		
		RestUtil rest = new RestUtil();
		
		rest.setURLBase("http://disa-api-integ:8080/services/viralloads");
		rest.setUsername("disa");
		rest.setPassword("disa");
		
		String jsonViralLoadInfo = null; 
		
		Gson gson = new Gson();
		
		try {
			jsonViralLoadInfo = rest.getRequestGet("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Type disaListType = new TypeToken<ArrayList<Disa>>(){}.getType();
		
		disaList = gson.fromJson(jsonViralLoadInfo, disaListType); 
		
		return disaList; 
	}
	
	public Date getArtStartDate(String uuid) {
		return null;
	}
	
	public String getUuidByNid(String nid) {
		return null;
	}
	
	public String getPatientName(){
		return null;
	}
	
	public String getPatientGender(){
		return null;
	}
	
	public String getRegimenUuid(){
		return null;
	}
}
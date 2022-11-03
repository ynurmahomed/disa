package org.openmrs.module.disa.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.FsrLog;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.DateUtil;
import org.openmrs.module.disa.extension.util.GenericUtil;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author machabane
 *
 */
public class ViralLoadFormSchedulerTask extends AbstractTask {
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
		System.out.println("module started...");
		Context.openSession();
		try {
			createViralLoadForm();
		} catch (HttpHostConnectException e) {
			//ignora a exception
		} catch (Exception e) {
			  String exceprionMessage = ExceptionUtils.getStackTrace(e); //send email
			  System.out.println("O erro "+exceprionMessage); 
			  //sendEmail(exceprionMessage, Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_TO).getPropertyValue());
		} finally {
			
		}
		Context.closeSession();
		System.out.println("module ended...");
	}
	
	@Transactional(rollbackFor=Exception.class )
	private void createViralLoadForm() throws Exception, HttpHostConnectException {

		// iterate the viral load list and create the encounters
		
		jsonViralLoad = getJsonViralLoad(); 
		System.out.println("There is " + jsonViralLoad.size() + " pending items to be processed");
		
		System.out.println("Syncing started...");
		
		 User user=Context.getUserService().getUserByUsername("generic.provider");			 
		 if(user==null) {				 
			 user= Context.getUserService().getUserByUsername("provedor.desconhecido");
		 }
		 
		 if(user==null) {
			 String message="O provedor generic.provider ou provedor.desconhecido nao foi encontrado no openmrs.";
			 System.out.println("O erro "+message); 
			 //sendEmail(message, Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_TO).getPropertyValue());
			 return;
		 }
		 
        Provider provider=Context.getProviderService().getProvidersByPerson(user.getPerson()).iterator().next();
		
		for (Disa disa : jsonViralLoad) {
		
		if(!disaService.existsByRequestId(disa.getRequestId())) {
			Encounter encounter = new Encounter();
			encounter.setEncounterDatetime(DateUtil.dateWithLeadingZeros()); 
			List<Patient> patientsByIdentifier = Context.getPatientService()
					.getPatients(null, disa.getNid().trim(), null, Boolean.TRUE);

			if (patientsByIdentifier.isEmpty()) {
				notProcessed = disa.getRequestId();
				updateNotProcessed();
				continue;
			} else {
				if (hasNoResult(disa)) {
					notProcessedNoResult = disa.getRequestId();
					updateNotProcessedNoResult();

					continue;
				} else {
					if (patientIds.size()>1) {
						//notify duplication
						String notification = "Os pacientes do OpenMRS com os Ids: "+Arrays.toString(patientIds.toArray())+" partilham o mesmo NID: "+disa.getNid().trim();
						System.out.println("O erro "+notification); 
						//sendEmail(notification, Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_OTHERS_TO).getPropertyValue());
						notProcessedDuplicateNid = disa.getRequestId();
						updateNotProcessedDuplicateNid();
						continue;
					} else if (hasNoResult(disa)) {
						notProcessedNoResult = disa.getRequestId();
						updateNotProcessedNoResult();
						continue;
					} else if(!GenericUtil.isNumeric(disa.getFinalViralLoadResult().trim())
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
						Context.getEncounterService().getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE));
				encounter.setLocation(locationBySismaCode);
				encounter.setForm(Context.getFormService().getFormByUuid(Constants.DISA_FORM));
	
				encounter.setProvider(Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), provider);
					
				Context.getEncounterService().saveEncounter(encounter);
	
				Obs obs_23835 = new Obs();
				obs_23835.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23835.setObsDatetime(new Date());
				obs_23835.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_NUMBER));
				obs_23835.setLocation(locationBySismaCode);
				obs_23835.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getHealthFacilityLabCode())) {//RequestingFacilityCode
					obs_23835.setValueText(disa.getHealthFacilityLabCode());
					Context.getObsService().saveObs(obs_23835, "");
				}
	
				Obs obs_23883 = new Obs();
				obs_23883.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23883.setObsDatetime(new Date());
				obs_23883.setConcept(Context.getConceptService().getConceptByUuid(Constants.PICKING_LOCATION));
				obs_23883.setLocation(locationBySismaCode);
				obs_23883.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getRequestingFacilityName())) {//RequestingFacilityName
					obs_23883.setValueText(disa.getRequestingFacilityName());
					Context.getObsService().saveObs(obs_23883, "");
				}
	
				Obs obs_23836 = new Obs();
				obs_23836.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23836.setObsDatetime(new Date());
				obs_23836.setConcept(Context.getConceptService().getConceptByUuid(Constants.ENCOUNTER_SERVICE));
				obs_23836.setLocation(locationBySismaCode);
				obs_23836.setEncounter(encounter);
				if (!(disa.getEncounter()==null) && StringUtils.isNotEmpty(GenericUtil.wardSelection(disa.getEncounter().trim()))) {//WARD
					obs_23836.setValueCoded(Context.getConceptService()
							.getConceptByName(GenericUtil.wardSelection(disa.getEncounter().trim())));
					Context.getObsService().saveObs(obs_23836, "");
				}
	
				Obs obs_1982 = new Obs();
				obs_1982.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_1982.setObsDatetime(new Date());
				obs_1982.setConcept(Context.getConceptService().getConceptByUuid(Constants.PREGNANT));
				obs_1982.setLocation(locationBySismaCode);
				obs_1982.setEncounter(encounter);
				if (disa.getPregnant().trim().equalsIgnoreCase(Constants.YES) || disa.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {//Pregnant
					obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.GESTATION));
					Context.getObsService().saveObs(obs_1982, "");
				} else if (disa.getPregnant().trim().equalsIgnoreCase(Constants.NO) || StringUtils.stripAccents(disa.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
					obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NO));
					Context.getObsService().saveObs(obs_1982, "");
				} else {
					obs_1982.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					Context.getObsService().saveObs(obs_1982, "");
				}
	
				Obs obs_6332 = new Obs();
				obs_6332.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_6332.setObsDatetime(new Date());
				obs_6332.setConcept(Context.getConceptService().getConceptByUuid(Constants.LACTATION));
				obs_6332.setLocation(locationBySismaCode);
				obs_6332.setEncounter(encounter);
				if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES) || disa.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {//BreastFeeding
					obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_YES));
					Context.getObsService().saveObs(obs_6332, "");
				} else if (disa.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO) || StringUtils.stripAccents(disa.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
					obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NO));
					Context.getObsService().saveObs(obs_6332, "");
				} else {
					obs_6332.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					Context.getObsService().saveObs(obs_6332, "");
				}
	
				Obs obs_23818 = new Obs();
				obs_23818.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23818.setObsDatetime(new Date());
				obs_23818.setConcept(Context.getConceptService().getConceptByUuid(Constants.REASON_FOR_TEST));
				obs_23818.setLocation(locationBySismaCode);
				obs_23818.setEncounter(encounter);
				if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {//ReasonForTest
					obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.ROUTINE_VIRAL_LOAD));
					Context.getObsService().saveObs(obs_23818, "");
				} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) {
					obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.REGIMEN_FAILURE));
					Context.getObsService().saveObs(obs_23818, "");
				} else if (disa.getReasonForTest().trim().equalsIgnoreCase(Constants.RAB)) {
					obs_23818.setValueCoded(
							Context.getConceptService().getConceptByUuid(Constants.REPEAT_AFTER_BREASTFEEDING));
					Context.getObsService().saveObs(obs_23818, "");
				} else {
					obs_23818.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
					Context.getObsService().saveObs(obs_23818, "");
				}
	
				Obs obs_23821 = new Obs();
				obs_23821.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23821.setObsDatetime(new Date());
				obs_23821.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
				obs_23821.setLocation(locationBySismaCode);
				obs_23821.setEncounter(encounter);
				if (!(disa.getHarvestDate()==null) && StringUtils.isNotEmpty(disa.getHarvestDate())) {//SpecimenDatetime
					obs_23821.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
					Context.getObsService().saveObs(obs_23821, "");
				}
	
				Obs obs_23824 = new Obs();
				obs_23824.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23824.setObsDatetime(new Date());
				obs_23824.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD));
				obs_23824.setLocation(locationBySismaCode);
				obs_23824.setEncounter(encounter);
				if (!(disa.getHarvestType()==null)) {
					if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PV)) {//TypeOfSampleCollection
						obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.VENOUS_PUNCTURE));
						Context.getObsService().saveObs(obs_23824, "");
					} else if (GenericUtil.removeAccents(disa.getHarvestType().trim()).equalsIgnoreCase(Constants.PD)) {
						obs_23824.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.DIGITAL_PUNCTURE));
						Context.getObsService().saveObs(obs_23824, "");
					}
				}
	
				Obs obs_23826 = new Obs();
				obs_23826.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23826.setObsDatetime(new Date());
				obs_23826.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
				obs_23826.setLocation(locationBySismaCode);
				obs_23826.setEncounter(encounter);
				if (!(disa.getDateOfSampleReceive()==null) && StringUtils.isNotEmpty(disa.getDateOfSampleReceive())) {//ReceivedDateTime
					obs_23826.setValueDate(DateUtil.deserialize(disa.getDateOfSampleReceive()));
					Context.getObsService().saveObs(obs_23826, "");
				}
	
				Obs obs_23833 = new Obs();
				obs_23833.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23833.setObsDatetime(new Date());
				obs_23833.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
				obs_23833.setLocation(locationBySismaCode);
				obs_23833.setEncounter(encounter);
				if (!(disa.getProcessingDate()==null) && StringUtils.isNotEmpty(disa.getProcessingDate())) {//AnalysisDateTime
					obs_23833.setValueDate(DateUtil.deserialize(disa.getProcessingDate()));
					Context.getObsService().saveObs(obs_23833, "");
				}
				
				//LastViralLoadResult & LastViralLoadDate
				Obs obs_165314 = new Obs();
				obs_165314.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_165314.setObsDatetime(new Date());
				obs_165314.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAST_VIRALLOAD_RESULT));
				obs_165314.setLocation(locationBySismaCode);
				obs_165314.setEncounter(encounter);
				obs_165314.setValueText(disa.getLastViralLoadResult());
				if (DateUtil.isValidDate(disa.getLastViralLoadDate())) { 
					obs_165314.setObsDatetime((DateUtil.string_To_Date(disa.getLastViralLoadDate()))); 
					Context.getObsService().saveObs(obs_165314, "");
				}
				
				//PrimeiraLinha & SegundaLinha
				Obs obs_21151 = new Obs();
				obs_21151.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_21151.setObsDatetime(new Date());
				obs_21151.setConcept(Context.getConceptService().getConceptByUuid(Constants.LINHA_TERAPEUTICA));
				obs_21151.setLocation(locationBySismaCode);
				obs_21151.setEncounter(encounter);
				if (disa.getPrimeiraLinha().equalsIgnoreCase(Constants.SIM)) {
					obs_21151.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.PRIMEIRA_LINHA));
					Context.getObsService().saveObs(obs_21151, "");
				}else if (disa.getSegundaLinha().equalsIgnoreCase(Constants.SIM)) { 
					obs_21151.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.SEGUNDA_LINHA));
					Context.getObsService().saveObs(obs_21151, "");
				}
				
				//ARTRegimen
				Obs obs_165315 = new Obs();
				obs_165315.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_165315.setObsDatetime(new Date());
				obs_165315.setConcept(Context.getConceptService().getConceptByUuid(Constants.ART_REGIMEN));
				obs_165315.setLocation(locationBySismaCode);
				obs_165315.setEncounter(encounter);
				if (StringUtils.isNotEmpty(disa.getArtRegimen())) { 
					obs_165315.setValueText(disa.getArtRegimen());
					Context.getObsService().saveObs(obs_165315, "");
				}
				
				//DataDeInicioDoTARV
				Obs obs_1190 = new Obs();
				obs_1190.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_1190.setObsDatetime(new Date());
				obs_1190.setConcept(Context.getConceptService().getConceptByUuid(Constants.ART_START_DATE));
				obs_1190.setLocation(locationBySismaCode);
				obs_1190.setEncounter(encounter);
				if (DateUtil.isValidDate(disa.getDataDeInicioDoTARV())) { 
					obs_1190.setValueDatetime(DateUtil.string_To_Date(disa.getDataDeInicioDoTARV()));    
					Context.getObsService().saveObs(obs_1190, "");
				}
				
				//SpecimenDatetime
				Obs obs_23840 = new Obs();
				obs_23840.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23840.setObsDatetime(new Date());
				obs_23840.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_REQUEST_DATE));
				obs_23840.setLocation(locationBySismaCode);
				obs_23840.setEncounter(encounter);
				if (!(disa.getHarvestDate()==null) && StringUtils.isNotEmpty(disa.getHarvestDate())) {
					obs_23840.setValueDate(DateUtil.deserialize(disa.getHarvestDate()));
					Context.getObsService().saveObs(obs_23840, "");
				}
				
				//LIMSSpecimenSourceCode
				Obs obs_23832 = new Obs();
				obs_23832.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23832.setObsDatetime(new Date());
				obs_23832.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_TYPE));
				obs_23832.setLocation(locationBySismaCode);
				obs_23832.setEncounter(encounter);
				if (!(disa.getSampleType()==null) && disa.getSampleType().trim().equalsIgnoreCase(Constants.DRYBLOODSPOT)) {
					obs_23832.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.DRY_BLOOD_SPOT));
					Context.getObsService().saveObs(obs_23832, "");
				} else if (!(disa.getSampleType()==null) && disa.getSampleType().trim().equalsIgnoreCase(Constants.PLASMA)) {
					obs_23832.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.PLASMA_));
					Context.getObsService().saveObs(obs_23832, "");
				}
				
				//Using FinalViralLoadResult column only
				//field: Cópias/ml
				if (GenericUtil.isNumeric(disa.getFinalViralLoadResult().trim())) {
						Obs obs_856 = new Obs();
						obs_856.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
						obs_856.setObsDatetime(new Date());
						obs_856.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
						obs_856.setLocation(locationBySismaCode);
						obs_856.setEncounter(encounter);
						obs_856.setValueNumeric(Double.valueOf(disa.getFinalViralLoadResult().trim()));
						Context.getObsService().saveObs(obs_856, ""); 
				} else
				
				//field: dropbox with answer label Indectetável
				if (disa.getFinalViralLoadResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL)) { 
					Obs obs_1306 = new Obs();
					obs_1306.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
					obs_1306.setObsDatetime(new Date());
					obs_1306.setConcept(Context.getConceptService().getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
					obs_1306.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD));
					obs_1306.setLocation(locationBySismaCode);
					obs_1306.setEncounter(encounter);
					Context.getObsService().saveObs(obs_1306, "");
				} else 
				
				//field: dropbox with answer <
				if (disa.getFinalViralLoadResult().contains(Constants.LESS_THAN)) { 
					Obs obs_1306 = new Obs();
					obs_1306.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
					obs_1306.setObsDatetime(new Date());
					obs_1306.setConcept(Context.getConceptService().getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
					obs_1306.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.LESSTHAN));
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
					Context.getObsService().saveObs(obs_1306, "");
				}
					
				Obs obs_23839 = new Obs();
				obs_23839.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23839.setObsDatetime(new Date());
				obs_23839.setConcept(Context.getConceptService().getConceptByUuid(Constants.APPROVED_BY));
				obs_23839.setEncounter(encounter);
				obs_23839.setLocation(locationBySismaCode);
				if (!(disa.getAprovedBy()==null) && StringUtils.isNotEmpty(disa.getAprovedBy().trim())) {
					obs_23839.setValueText(disa.getAprovedBy().trim());
					Context.getObsService().saveObs(obs_23839, "");
				}
	
				Obs obs_23841 = new Obs();
				obs_23841.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_23841.setObsDatetime(new Date());
				obs_23841.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_COMMENTS));
				obs_23841.setLocation(locationBySismaCode);
				obs_23841.setEncounter(encounter);
				if (!(disa.getLabComments()==null) && StringUtils.isNotEmpty(disa.getLabComments().trim())) {
					obs_23841.setValueText(disa.getLabComments().trim());
					Context.getObsService().saveObs(obs_23841, "");
				}
	
				Obs obs_22771 = new Obs();
				obs_22771.setPerson(Context.getPersonService().getPersonByUuid(lstPatient.get(0).getUuid()));
				obs_22771.setObsDatetime(new Date());
				obs_22771.setConcept(Context.getConceptService().getConceptByUuid(Constants.ORDER_ID));
				obs_22771.setLocation(locationBySismaCode);
				obs_22771.setEncounter(encounter);
				if (!(disa.getRequestId()==null) && StringUtils.isNotEmpty(disa.getRequestId().trim())) {
					obs_22771.setValueText(disa.getRequestId().trim());
					Context.getObsService().saveObs(obs_22771, "");
				}
				
				//log the fsr in openmrs
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

			Obs obs_23826 = new Obs();
			obs_23826.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23826.setObsDatetime(new Date());
			obs_23826.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
			obs_23826.setLocation(locationBySismaCode);
			obs_23826.setEncounter(encounter);
			if (!(disa.getDateOfSampleReceive()==null) && StringUtils.isNotEmpty(disa.getDateOfSampleReceive())) {//ReceivedDateTime
				obs_23826.setValueDate(DateUtil.deserialize(disa.getDateOfSampleReceive()));
				Context.getObsService().saveObs(obs_23826, "");
			}

			Obs obs_23833 = new Obs();
			obs_23833.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23833.setObsDatetime(new Date());
			obs_23833.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
			obs_23833.setLocation(locationBySismaCode);
			obs_23833.setEncounter(encounter);
			if (!(disa.getProcessingDate()==null) && StringUtils.isNotEmpty(disa.getProcessingDate())) {//AnalysisDateTime
				obs_23833.setValueDate(DateUtil.deserialize(disa.getProcessingDate()));
				Context.getObsService().saveObs(obs_23833, "");
			}

			Obs obs_23832 = new Obs();
			obs_23832.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23832.setObsDatetime(new Date());
			obs_23832.setConcept(Context.getConceptService().getConceptByUuid(Constants.SAMPLE_TYPE));
			obs_23832.setLocation(locationBySismaCode);
			obs_23832.setEncounter(encounter);
			if (!(disa.getSampleType()==null) && disa.getSampleType().trim().equalsIgnoreCase(Constants.DBS)) {//LIMSSpecimenSourceDesc
				obs_23832.setValueCoded(Context.getConceptService().getConceptByUuid(Constants.DRY_BLOOD_SPOT));
				Context.getObsService().saveObs(obs_23832, "");
			}

			Obs obs_856 = new Obs();
			obs_856.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_856.setObsDatetime(new Date());
			obs_856.setConcept(Context.getConceptService().getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
			obs_856.setLocation(locationBySismaCode);
			obs_856.setEncounter(encounter);
			if (!(disa.getHivViralLoadResult()==null) && StringUtils.isNotEmpty(disa.getHivViralLoadResult())) {

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
			} else if (!(disa.getViralLoadResultCopies()==null) && StringUtils.isNotEmpty(disa.getViralLoadResultCopies())
					&& NumberUtils.isNumber(disa.getViralLoadResultCopies().trim())) {
				obs_856.setValueNumeric(Double.valueOf(disa.getViralLoadResultCopies().trim()));
				Context.getObsService().saveObs(obs_856, "");
			} else if (!(disa.getViralLoadResultCopies()==null) && StringUtils.isNotEmpty(disa.getViralLoadResultCopies())
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
			
			if(disa.getViralLoadResultLog() == null){
				obs_165243.setValueNumeric(Double.valueOf(0.0));
			} else if (!(disa.getViralLoadResultLog()==null) && disa.getViralLoadResultLog().contains(">") ){
				obs_165243.setValueNumeric(Double.valueOf(disa.getViralLoadResultLog().toString().replace(">", "").trim()));
			} else if (!(disa.getViralLoadResultLog()==null) && disa.getViralLoadResultLog().contains("<") ){
				obs_165243.setValueNumeric(Double.valueOf(disa.getViralLoadResultLog().toString().replace("<", "").trim()));
			} else {
				obs_165243.setValueNumeric(Double.valueOf(disa.getViralLoadResultLog()));
			}

			Context.getObsService().saveObs(obs_165243, "");

			Obs obs_23839 = new Obs();
			obs_23839.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23839.setObsDatetime(new Date());
			obs_23839.setConcept(Context.getConceptService().getConceptByUuid(Constants.APPROVED_BY));
			obs_23839.setEncounter(encounter);
			obs_23839.setLocation(locationBySismaCode);
			if (!(disa.getAprovedBy()==null) && StringUtils.isNotEmpty(disa.getAprovedBy().trim())) {
				obs_23839.setValueText(disa.getAprovedBy().trim());
				Context.getObsService().saveObs(obs_23839, "");
			}

			Obs obs_23841 = new Obs();
			obs_23841.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_23841.setObsDatetime(new Date());
			obs_23841.setConcept(Context.getConceptService().getConceptByUuid(Constants.LAB_COMMENTS));
			obs_23841.setLocation(locationBySismaCode);
			obs_23841.setEncounter(encounter);
			if (!(disa.getLabComments()==null) && StringUtils.isNotEmpty(disa.getLabComments().trim())) {
				obs_23841.setValueText(disa.getLabComments().trim());
				Context.getObsService().saveObs(obs_23841, "");
			}

			Obs obs_22771 = new Obs();
			obs_22771.setPerson(Context.getPersonService().getPersonByUuid(patientsByIdentifier.get(0).getUuid()));
			obs_22771.setObsDatetime(new Date());
			obs_22771.setConcept(Context.getConceptService().getConceptByUuid(Constants.ORDER_ID));
			obs_22771.setLocation(locationBySismaCode);
			obs_22771.setEncounter(encounter);
			if (!(disa.getRequestId()==null) && StringUtils.isNotEmpty(disa.getRequestId().trim())) {
				obs_22771.setValueText(disa.getRequestId().trim());
				Context.getObsService().saveObs(obs_22771, "");
			}
			
			//log the fsr in openmrs
			FsrLog fsrLog = new FsrLog();
			fsrLog.setPatientId(encounter.getPatient().getPatientId());
			fsrLog.setEncounterId(encounter.getEncounterId());
			fsrLog.setPatientIdentifier(disa.getNid());
			fsrLog.setRequestId(disa.getRequestId());
			fsrLog.setCreator(Context.getAuthenticatedUser().getId());     
			fsrLog.setDateCreated(new Date());
			disaService.saveFsrLog(fsrLog);					
			
			}
		
		  updateProcessed();
		}
		
		lstPatient.clear();
		jsonViralLoad.clear();
		patientIds.clear(); 
		
		System.out.println("Syncing ended...");
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
		LocationAttributeType locationAttributeType = Context.getLocationService()
				.getLocationAttributeTypeByUuid(Context.getAdministrationService().
						getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID).getPropertyValue());
		Map<LocationAttributeType, Object> hashMap = new HashMap<LocationAttributeType, Object>();
		hashMap.put(locationAttributeType, sismaCode);
		List<Location> locations = Context.getLocationService().getLocations(null, null, hashMap, false, null, null);

		return locations.get(0);
	}

	private List<String> getAllDisaSismaCodes() {
		List<String> sismaCodes = Arrays.asList(Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue().split(",")); 
		return sismaCodes;
	}
	
	private String getDisaProvince() {
		return Context.getAdministrationService()
		.getGlobalPropertyObject(Constants.DISA_PROVINCE).getPropertyValue();
	}
	
	@SuppressWarnings("unused")
	private void sendEmail(final String message, final String mailTo) {
		 /*GenericUtil.SendMail(mailTo,
				  Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM).getPropertyValue(),
				  Constants.DISA_MAIL_SUBJECT
				  +Context.getLocationService().getDefaultLocation().getDescription(),
				  Constants.DISA_MAIL_ERROR
				  +new SimpleDateFormat("dd/MM/yyyy").format(new Date())+"\n\n\n" + message,
				  Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_HOST).getPropertyValue(), 
				  Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PASSWORD).getPropertyValue(), 
				  Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PORT).getPropertyValue());*/
		 
		 GenericUtil.sendMail
		 		(
				 mailTo.split(","), 
				 Constants.DISA_MAIL_SUBJECT+Context.getLocationService().getDefaultLocation().getDescription(), 
				 message, 
				 Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM).getPropertyValue(), 
 				 Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_HOST).getPropertyValue(), 
				 Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PORT).getPropertyValue(), 
				 Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_MAIL_FROM_PASSWORD).getPropertyValue()
				 );
	}
}
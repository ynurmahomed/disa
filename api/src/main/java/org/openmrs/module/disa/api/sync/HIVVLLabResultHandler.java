package org.openmrs.module.disa.api.sync;

import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.HIVVLLabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;
import org.openmrs.module.disa.NotProcessingCause;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.api.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class HIVVLLabResultHandler extends BaseLabResultHandler {

    private EncounterService encounterService;

    private FormService formService;

    private PersonService personService;

    private ConceptService conceptService;

    @Autowired
    public HIVVLLabResultHandler(
            @Qualifier("encounterService") EncounterService encounterService,
            @Qualifier("formService") FormService formService,
            @Qualifier("personService") PersonService personService,
            @Qualifier("conceptService") ConceptService conceptService,
            @Qualifier("obsService") ObsService obsService) {

        this.encounterService = encounterService;
        this.formService = formService;
        this.personService = personService;
        this.conceptService = conceptService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {

        if (labResult.isPending() && (labResult instanceof HIVVLLabResult)) {

            Patient patient = (Patient) getSyncContext().get(PatientNidLookup.PATIENT_KEY);
            if (patient == null) {
                throw new DisaModuleAPIException(PatientNidLookup.PATIENT_KEY + " is missing from the sync context");
            }

            Provider provider = (Provider) getSyncContext().get(ProviderLookup.PROVIDER_KEY);
            if (provider == null) {
                throw new DisaModuleAPIException(ProviderLookup.PROVIDER_KEY + " is missing from the sync context");
            }

            Location location = (Location) getSyncContext().get(LocationLookup.LOCATION_KEY);
            if (location == null) {
                throw new DisaModuleAPIException(
                        LocationLookup.LOCATION_KEY + " is missing from the sync context");
            }

            HIVVLLabResult vl = (HIVVLLabResult) labResult;

            validateResult(vl);

            if (LabResultStatus.NOT_PROCESSED != labResult.getLabResultStatus()) {
                Encounter encounter = new Encounter();
                Date dateWithLeadingZeros = DateUtil.dateWithLeadingZeros();
                EncounterType encounterType = encounterService.getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE);
                EncounterRole encounterRole = encounterService
                        .getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);

                encounter.setEncounterDatetime(dateWithLeadingZeros);
                encounter.setPatient(patient);
                encounter.setEncounterType(encounterType);
                encounter.setLocation(location);
                encounter.setForm(formService.getFormByUuid(Constants.DISA_FORM));
                encounter.setProvider(encounterRole, provider);

                createFsrObs(vl, encounter);

                labResult.setLabResultStatus(LabResultStatus.PROCESSED);
                getSyncContext().put(ENCOUNTER_KEY, encounter);
            }

        }

        return super.handle(labResult);
    }

    private void validateResult(HIVVLLabResult vl) {
        if (hasNoResult(vl)) {

            updateNotProcessed(vl, NotProcessingCause.INVALID_RESULT);

        } else if (!isNumeric(vl.getFinalResult().trim())
                && !(vl.getFinalResult().contains(Constants.LESS_THAN))
                && !(vl.getFinalResult().contains(Constants.MORE_THAN))
                && !(vl.getFinalResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL))) {

            updateNotProcessed(vl, NotProcessingCause.INVALID_RESULT);

        } else if (vl.getFinalResult().contains(Constants.MORE_THAN)
                || vl.getFinalResult().contains(Constants.LESS_THAN)) {
            String finalResult = vl.getFinalResult();
            String maybeNumeric = removeAllExceptNumericPart(
                    finalResult.substring(finalResult.indexOf(Constants.MORE_THAN) + 1));
            if (!isNumeric(maybeNumeric)) {
                updateNotProcessed(vl, NotProcessingCause.INVALID_RESULT);
            }
        }
    }

    private String removeAllExceptNumericPart(String result) {
        return result
                .toLowerCase()
                .replaceAll("\\s", "")
                .replace(Constants.LESS_THAN, "")
                .replace(Constants.MORE_THAN, "")
                .replace(Constants.COPIES, "")
                .replace("copies", "")
                .replace("cp", "")
                .replace(Constants.FORWARD_SLASH, "")
                .replace(Constants.ML, "");
    }

    private void createFsrObs(HIVVLLabResult labResult, Encounter encounter) {
        Obs obs23835 = new Obs();
        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        // Request ID
        Obs obs22771 = new Obs();
        obs22771.setPerson(person);
        obs22771.setObsDatetime(obsDatetime);
        obs22771.setConcept(conceptService.getConceptByUuid(Constants.ORDER_ID));
        obs22771.setLocation(location);
        obs22771.setEncounter(encounter);
        if (!(labResult.getRequestId() == null) && StringUtils.isNotEmpty(labResult.getRequestId().trim())) {
            obs22771.setValueText(labResult.getRequestId().trim());
            encounter.addObs(obs22771);
        }

        // Requesting Laboratory ID
        obs23835.setPerson(person);
        obs23835.setObsDatetime(obsDatetime);
        obs23835.setConcept(conceptService.getConceptByUuid(Constants.LAB_NUMBER));
        obs23835.setLocation(location);
        obs23835.setEncounter(encounter);
        if (StringUtils.isNotEmpty(labResult.getHealthFacilityLabCode())) {
            obs23835.setValueText(labResult.getHealthFacilityLabCode());
            encounter.addObs(obs23835);
        }

        // Currently pregnant?
        Obs obs1982 = new Obs();
        obs1982.setPerson(person);
        obs1982.setObsDatetime(obsDatetime);
        obs1982.setConcept(conceptService.getConceptByUuid(Constants.PREGNANT));
        obs1982.setLocation(location);
        obs1982.setEncounter(encounter);
        if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.YES)
                || labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// Pregnant
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.GESTATION));
            encounter.addObs(obs1982);
        } else if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(labResult.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
            encounter.addObs(obs1982);
        } else {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
            encounter.addObs(obs1982);
        }

        // Currently breastfeeding?
        Obs obs6332 = new Obs();
        obs6332.setPerson(person);
        obs6332.setObsDatetime(obsDatetime);
        obs6332.setConcept(conceptService.getConceptByUuid(Constants.LACTATION));
        obs6332.setLocation(location);
        obs6332.setEncounter(encounter);
        if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
                || labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_YES));
            encounter.addObs(obs6332);
        } else if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(labResult.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
            encounter.addObs(obs6332);
        } else {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
            encounter.addObs(obs6332);
        }

        // Requesting facility name
        Obs obs23883 = new Obs();
        obs23883.setPerson(person);
        obs23883.setObsDatetime(obsDatetime);
        obs23883.setConcept(conceptService.getConceptByUuid(Constants.PICKING_LOCATION));
        obs23883.setLocation(location);
        obs23883.setEncounter(encounter);
        if (StringUtils.isNotEmpty(labResult.getRequestingFacilityName())) {
            obs23883.setValueText(labResult.getRequestingFacilityName());
            encounter.addObs(obs23883);
        }

        // Specimen collection Date
        Obs obs23821 = new Obs();
        obs23821.setPerson(person);
        obs23821.setObsDatetime(obsDatetime);
        obs23821.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
        obs23821.setLocation(location);
        obs23821.setEncounter(encounter);
        if (labResult.getHarvestDate() != null) {
            obs23821.setValueDate(DateUtil.toDate(labResult.getHarvestDate()));
            encounter.addObs(obs23821);
        }

        // Specimen registration date
        if (labResult.getRegisteredDateTime() != null) {
            Concept sampleCollectionDate = conceptService.getConceptByUuid(Constants.SAMPLE_REGISTRATION_DATE);
            Obs obs165461 = new Obs(person, sampleCollectionDate, obsDatetime, location);
            obs165461.setValueDate(DateUtil.toDate(labResult.getRegisteredDateTime()));
            encounter.addObs(obs165461);
        }

        // Result authorization date
        if (labResult.getLabResultDate() != null) {
            Concept resultApprovalDate = conceptService.getConceptByUuid(Constants.RESULT_APPROVAL_DATE);
            Obs obs165462 = new Obs(person, resultApprovalDate, obsDatetime, location);
            obs165462.setValueDate(DateUtil.toDate(labResult.getLabResultDate()));
            encounter.addObs(obs165462);
        }

        // Reason for requesting viral load
        Obs obs23818 = new Obs();
        obs23818.setPerson(person);
        obs23818.setObsDatetime(obsDatetime);
        obs23818.setConcept(conceptService.getConceptByUuid(Constants.REASON_FOR_TEST));
        obs23818.setLocation(location);
        obs23818.setEncounter(encounter);
        if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {
            obs23818.setValueCoded(conceptService.getConceptByUuid(Constants.ROUTINE_VIRAL_LOAD));
            encounter.addObs(obs23818);
        } else if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.SUSPECTED_TREATMENT_FAILURE)) {
            obs23818.setValueCoded(conceptService.getConceptByUuid(Constants.REGIMEN_FAILURE));
            encounter.addObs(obs23818);
        } else if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.RAB)) {
            obs23818.setValueCoded(
                    conceptService.getConceptByUuid(Constants.REPEAT_AFTER_BREASTFEEDING));
            encounter.addObs(obs23818);
        } else {
            obs23818.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
            encounter.addObs(obs23818);
        }

        // Using FinalViralLoadResult column only
        // field: Cópias/ml
        if (isNumeric(labResult.getFinalResult().trim())) {
            Obs obs856 = new Obs();
            obs856.setPerson(person);
            obs856.setObsDatetime(obsDatetime);
            obs856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
            obs856.setLocation(location);
            obs856.setEncounter(encounter);
            obs856.setValueNumeric(Double.valueOf(labResult.getFinalResult().trim()));
            encounter.addObs(obs856);
        } else if (labResult.getFinalResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL)) {
            // field: dropbox with answer label Indectetável
            Obs obs1305 = new Obs();
            obs1305.setPerson(person);
            obs1305.setObsDatetime(obsDatetime);
            obs1305.setConcept(
                    conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
            obs1305.setValueCoded(
                    conceptService.getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD));
            obs1305.setLocation(location);
            obs1305.setEncounter(encounter);
            encounter.addObs(obs1305);
        } else if (labResult.getFinalResult().contains(Constants.LESS_THAN)) {
            // field: dropbox with answer <
            Obs obs1305 = new Obs();
            obs1305.setPerson(person);
            obs1305.setObsDatetime(obsDatetime);
            obs1305.setConcept(
                    conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
            obs1305.setValueCoded(conceptService.getConceptByUuid(Constants.LESSTHAN));
            obs1305.setLocation(location);
            obs1305.setEncounter(encounter);
            String finalResult = labResult.getFinalResult();
            obs1305.setComment(removeAllExceptNumericPart(finalResult));
            encounter.addObs(obs1305);
        } else if (labResult.getFinalResult().contains(Constants.MORE_THAN)) {
            Obs obs856 = new Obs();
            obs856.setPerson(person);
            obs856.setObsDatetime(obsDatetime);
            obs856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
            obs856.setLocation(location);
            obs856.setEncounter(encounter);
            String finalResult = labResult.getFinalResult();
            String numericPart = removeAllExceptNumericPart(
                    finalResult.substring(finalResult.indexOf(Constants.MORE_THAN) + 1));
            obs856.setValueNumeric(Double.valueOf(numericPart));
            encounter.addObs(obs856);
        }
    }

    private void updateNotProcessed(LabResult labResult, NotProcessingCause cause) {
        labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
        labResult.setNotProcessingCause(cause);
    }

    private boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    private boolean hasNoResult(HIVVLLabResult vl) {
        return (vl.getFinalResult() == null || vl.getFinalResult().isEmpty());
    }
}

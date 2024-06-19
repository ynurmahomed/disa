package org.openmrs.module.disa.api.sync;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.SampleType;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class HIVVLLabResultHandler extends BaseLabResultHandler {

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
                Encounter encounter = getElabEncounter(patient, provider, location, vl);

                addVlObs(vl, encounter);

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

    private void addVlObs(HIVVLLabResult labResult, Encounter encounter) {
        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

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

        // Specimen type
        SampleType sampleType = labResult.getSampleType();
        List<SampleType> validSampleTypes = Arrays.asList(SampleType.DBS, SampleType.PL, SampleType.PSC);
        if (sampleType != null && validSampleTypes.contains(sampleType)) {
            Concept concept = conceptService.getConceptByUuid(Constants.SAMPLE_TYPE);
            Obs obs23832 = new Obs(person, concept, obsDatetime, location);
            obs23832.setValueCoded(conceptService.getConceptByUuid(sampleType.getConceptUuid()));
            encounter.addObs(obs23832);
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

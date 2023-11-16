package org.openmrs.module.disa.api.sync;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.TBLamLabResult;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class TBLamLabResultHandler extends BaseLabResultHandler {

    @Override
    public LabResultStatus handle(LabResult labResult) {
        if (labResult.isPending() && (labResult instanceof TBLamLabResult)) {

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

            TBLamLabResult tbLam = (TBLamLabResult) labResult;
            boolean valid = false;
            if (tbLam.getFinalResult() != null && !tbLam.getFinalResult().isEmpty()) {
                boolean negative = tbLam.getFinalResult().contains("Negativ");
                boolean positive = tbLam.getFinalResult().contains("Positiv");

                valid = negative || positive;

                if (valid) {
                    Encounter encounter = getElabEncounter(patient, provider, location, tbLam);

                    addTbLamObs(tbLam, encounter, negative);

                    tbLam.setLabResultStatus(LabResultStatus.PROCESSED);
                    getSyncContext().put(ENCOUNTER_KEY, encounter);
                }

            }

            if (!valid) {
                tbLam.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
                tbLam.setNotProcessingCause(NotProcessingCause.INVALID_RESULT);
            }

        }
        return super.handle(labResult);
    }

    private void addTbLamObs(TBLamLabResult tbLam, Encounter encounter, boolean negativeResult) {

        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        // TB LAM
        Concept tbLamConcept = conceptService.getConceptByUuid(Constants.TB_LAM);
        Obs obs23951 = new Obs(person, tbLamConcept, obsDatetime, location);
        if (negativeResult) {
            obs23951.setValueCoded(conceptService.getConceptByUuid(Constants.NEGATIVE));
        } else {
            obs23951.setValueCoded(conceptService.getConceptByUuid(Constants.POSITIVE));
            Concept answer = getPositivityLevelAnswer(tbLam.getPositivityLevel());
            if (answer != null) {
                Concept positivityLevel = conceptService.getConceptByUuid(Constants.POSITIVITY_LEVEL);
                Obs obs165185 = new Obs(person, positivityLevel, obsDatetime, location);
                obs165185.setValueCoded(answer);
                encounter.addObs(obs165185);
            }
        }
        encounter.addObs(obs23951);
    }

    private Concept getPositivityLevelAnswer(String fromString) {
        if (fromString == null) {
            return null;
        }
        switch (fromString) {
            case "GRAI":
                return conceptService.getConceptByUuid(Constants.LEVEL_1);
            case "GRAII":
                return conceptService.getConceptByUuid(Constants.LEVEL_2);
            case "GRIII":
                return conceptService.getConceptByUuid(Constants.LEVEL_3);
            case "GRIV":
                return conceptService.getConceptByUuid(Constants.LEVEL_4);
            default:
                return null;
        }
    }
}

package org.openmrs.module.disa.api.sync;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.disa.api.CRAGLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.SampleType;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class CRAGLabResultHandler extends BaseLabResultHandler {

    @Override
    public LabResultStatus handle(LabResult labResult) {
        if (labResult.isPending() && (labResult instanceof CRAGLabResult)) {

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

            CRAGLabResult crag = (CRAGLabResult) labResult;

            boolean valid = false;
            if (crag.getFinalResult() != null && !crag.getFinalResult().isEmpty()) {
                boolean negative = crag.getFinalResult().contains("Negativ");
                boolean positive = crag.getFinalResult().contains("Positiv");

                valid = negative || positive;

                if (valid) {
                    Encounter encounter = getElabEncounter(patient, provider, location, crag);

                    addCragObs(crag, encounter, negative);

                    crag.setLabResultStatus(LabResultStatus.PROCESSED);
                    getSyncContext().put(ENCOUNTER_KEY, encounter);
                }

            }

            if (!valid) {
                crag.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
                crag.setNotProcessingCause(NotProcessingCause.INVALID_RESULT);
            }

        }

        return super.handle(labResult);
    }

    private void addCragObs(CRAGLabResult crag, Encounter encounter, boolean negative) {
        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        Concept cragConcept = conceptService.getConceptByUuid(Constants.CRAG);
        Obs obs23952 = new Obs(person, cragConcept, obsDatetime, location);
        if (negative) {
            obs23952.setValueCoded(conceptService.getConceptByUuid(Constants.NEGATIVE));
        } else {
            obs23952.setValueCoded(conceptService.getConceptByUuid(Constants.POSITIVE));
        }
        encounter.addObs(obs23952);

        // Specimen type
        SampleType sampleType = crag.getSampleType();
        List<SampleType> validSampleTypes = Arrays.asList(SampleType.SER, SampleType.LCR);
        if (sampleType != null && validSampleTypes.contains(sampleType)) {
            Concept concept = conceptService.getConceptByUuid(Constants.SAMPLE_TYPE);
            Obs obs23832 = new Obs(person, concept, obsDatetime, location);
            obs23832.setValueCoded(conceptService.getConceptByUuid(sampleType.getConceptUuid()));
            encounter.addObs(obs23832);
        }
    }
}

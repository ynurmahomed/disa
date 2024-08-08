package org.openmrs.module.disa.api.sync;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.disa.api.CD4LabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CD4LabResultHandler extends BaseLabResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(CD4LabResultHandler.class);

    @Override
    public LabResultStatus handle(LabResult labResult) {
        if (labResult.isPending() && labResult instanceof CD4LabResult) {
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

            CD4LabResult cd4 = (CD4LabResult) labResult;

            validateResult(cd4);

            if (LabResultStatus.NOT_PROCESSED != cd4.getLabResultStatus()) {
                Encounter encounter = getElabEncounter(patient, provider, location, cd4);

                addCD4Obs(cd4, encounter);

                cd4.setLabResultStatus(LabResultStatus.PROCESSED);
                getSyncContext().put(ENCOUNTER_KEY, encounter);
            }
        }

        return super.handle(labResult);
    }

    private void validateResult(CD4LabResult cd4) {
        boolean valid = true;
        try {
            if (cd4.getFinalResult() == null || cd4.getFinalResult().isEmpty()) {
                valid = false;
            } else {
                String normalized = cd4.getFinalResult()
                        .toLowerCase()
                        .replaceAll("\\s", "")
                        .replace(Constants.LESS_THAN, "")
                        .replace(Constants.MORE_THAN, "")
                        .replace("cells", "")
                        .replace("celulas", "")
                        .replace("células", "")
                        .replace("cl", "")
                        .replace(Constants.FORWARD_SLASH, "")
                        .replace("ul", "");
                Integer.parseUnsignedInt(normalized);
            }
        } catch (NumberFormatException e) {
            valid = false;
        }

        if (!valid) {
            cd4.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
            cd4.setNotProcessingCause(NotProcessingCause.INVALID_RESULT);
        }
    }

    private void addCD4Obs(CD4LabResult cd4, Encounter encounter) {

        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        String normalized = cd4.getFinalResult()
                .toLowerCase()
                .replaceAll("\\s", "")
                .replace("cells", "")
                .replace("celulas", "")
                .replace("células", "")
                .replace("cl", "")
                .replace(Constants.FORWARD_SLASH, "")
                .replace("ul", "");

        // CD4 Semi-quantitative
        if (normalized.startsWith(Constants.MORE_THAN)) {
            Concept cd4SemiQuantitative = conceptService.getConceptByUuid(Constants.CD4_SEMI_QUANTITATIVE);
            Obs obs165515 = new Obs(person, cd4SemiQuantitative, obsDatetime, location);
            obs165515.setValueCoded(conceptService.getConceptByUuid(Constants.CD4_GREATER_THAN_200));
            encounter.addObs(obs165515);
        } else if (normalized.startsWith(Constants.LESS_THAN)) {
            Concept cd4SemiQuantitative = conceptService.getConceptByUuid(Constants.CD4_SEMI_QUANTITATIVE);
            Obs obs165515 = new Obs(person, cd4SemiQuantitative, obsDatetime, location);
            obs165515.setValueCoded(conceptService.getConceptByUuid(Constants.CD4_LESS_THAN_200));
            encounter.addObs(obs165515);
        } else {
            // CD4 Absolute
            Concept cd4Absolute = conceptService.getConceptByUuid(Constants.CD4_ABSOLUTE);
            Obs obs1695 = new Obs(person, cd4Absolute, obsDatetime, location);
            obs1695.setValueNumeric(Double.valueOf(normalized));
            encounter.addObs(obs1695);
        }

        // CD4 %
        if (StringUtils.isNotEmpty(cd4.getCd4Percentage())) {
            try {
                Concept cd4Percent = conceptService.getConceptByUuid(Constants.CD4_PERCENT);
                Obs obs730 = new Obs(person, cd4Percent, obsDatetime, location);
                obs730.setValueNumeric(Double.parseDouble(cd4.getCd4Percentage()));
                encounter.addObs(obs730);
            } catch (NumberFormatException e) {
                logger.debug("CD4 % is not a number: {}", cd4.getCd4Percentage());
            }
        }

    }

}

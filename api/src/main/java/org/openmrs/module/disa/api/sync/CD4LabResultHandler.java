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

            Integer result = null;
            try {
                result = Integer.parseUnsignedInt(cd4.getFinalResult().trim());
            } catch (NumberFormatException e) {
                cd4.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
                // TODO change to invalid result
                cd4.setNotProcessingCause(NotProcessingCause.INVALID_RESULT);
            }

            if (result != null) {
                Encounter encounter = getElabEncounter(patient, provider, location, cd4);

                addCD4Obs(cd4, encounter, result);

                cd4.setLabResultStatus(LabResultStatus.PROCESSED);
                getSyncContext().put(ENCOUNTER_KEY, encounter);
            }
        }

        return super.handle(labResult);
    }

    private void addCD4Obs(CD4LabResult cd4, Encounter encounter, Integer result) {

        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        // CD4 Absolute
        Concept cd4Absolute = conceptService.getConceptByUuid(Constants.CD4_ABSOLUTE);
        Obs obs1695 = new Obs(person, cd4Absolute, obsDatetime, location);
        obs1695.setValueNumeric(result.doubleValue());
        encounter.addObs(obs1695);

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

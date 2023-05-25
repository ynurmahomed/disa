package org.openmrs.module.disa.api.sync;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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
import org.openmrs.api.PersonService;
import org.openmrs.module.disa.CD4LabResult;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;
import org.openmrs.module.disa.NotProcessingCause;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.api.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CD4LabResultHandler extends BaseLabResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(CD4LabResultHandler.class);

    private EncounterService encounterService;

    private FormService formService;

    private ConceptService conceptService;

    private PersonService personService;

    @Autowired
    public CD4LabResultHandler(EncounterService encounterService, FormService formService,
            ConceptService conceptService, PersonService personService) {
        this.encounterService = encounterService;
        this.formService = formService;
        this.conceptService = conceptService;
        this.personService = personService;
    }

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
                Encounter encounter = new Encounter();
                EncounterType encounterType = encounterService.getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE);
                EncounterRole encounterRole = encounterService
                        .getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);

                LocalDateTime todayMidnight = LocalDateTime.now().with(LocalTime.MIDNIGHT);
                encounter.setEncounterDatetime(DateUtil.toDate(todayMidnight));
                encounter.setPatient(patient);
                encounter.setEncounterType(encounterType);
                encounter.setLocation(location);
                encounter.setForm(formService.getFormByUuid(Constants.DISA_FORM));
                encounter.setProvider(encounterRole, provider);

                createELabObs(cd4, encounter, result);

                cd4.setLabResultStatus(LabResultStatus.PROCESSED);
                getSyncContext().put(ENCOUNTER_KEY, encounter);
            }
        }

        return super.handle(labResult);
    }

    private void createELabObs(CD4LabResult cd4, Encounter encounter, Integer result) {

        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();
        Location location = encounter.getLocation();

        // Order Number
        Concept orderId = conceptService.getConceptByUuid(Constants.ORDER_ID);
        Obs obs22771 = new Obs(person, orderId, obsDatetime, location);
        obs22771.setValueText(cd4.getRequestId().trim());
        encounter.addObs(obs22771);

        // Lab Number
        if (StringUtils.isNotEmpty(cd4.getHealthFacilityLabCode())) {
            Concept labNumber = conceptService.getConceptByUuid(Constants.LAB_NUMBER);
            Obs obs23835 = new Obs(person, labNumber, obsDatetime, location);
            obs23835.setValueText(cd4.getHealthFacilityLabCode());
            encounter.addObs(obs23835);
        }

        // Pregnant
        Concept pregnant = conceptService.getConceptByUuid(Constants.PREGNANT);
        Obs obs1982 = new Obs(person, pregnant, obsDatetime, location);
        if (cd4.getPregnant().trim().equalsIgnoreCase(Constants.YES)
                || cd4.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.GESTATION));
        } else if (cd4.getPregnant().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(cd4.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
        } else {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
        }
        encounter.addObs(obs1982);

        // Breastfeeding
        Concept breastfeeding = conceptService.getConceptByUuid(Constants.LACTATION);
        Obs obs6332 = new Obs(person, breastfeeding, obsDatetime, location);
        if (cd4.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
                || cd4.getBreastFeeding().trim().equalsIgnoreCase(Constants.SIM)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_YES));
        } else if (cd4.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(cd4.getBreastFeeding().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
        } else {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
        }
        encounter.addObs(obs6332);

        // Picking location
        if (StringUtils.isNotEmpty(cd4.getRequestingFacilityName())) {
            Concept pickingLocation = conceptService.getConceptByUuid(Constants.PICKING_LOCATION);
            Obs obs23883 = new Obs(person, pickingLocation, obsDatetime, location);
            obs23883.setValueText(cd4.getRequestingFacilityName());
            encounter.addObs(obs23883);
        }

        // Sample collection date time
        if (cd4.getHarvestDate() != null) {
            Concept sampleCollectionDate = conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE);
            Obs obs23821 = new Obs(person, sampleCollectionDate, obsDatetime, location);
            obs23821.setValueDate(DateUtil.toDate(cd4.getHarvestDate()));
            encounter.addObs(obs23821);
        }

        // Sample regsitration date
        if (cd4.getRegisteredDateTime() != null) {
            Concept sampleCollectionDate = conceptService.getConceptByUuid(Constants.SAMPLE_REGISTRATION_DATE);
            Obs obs165461 = new Obs(person, sampleCollectionDate, obsDatetime, location);
            obs165461.setValueDate(DateUtil.toDate(cd4.getRegisteredDateTime()));
            encounter.addObs(obs165461);
        }

        // Result approved date
        if (cd4.getLabResultDate() != null) {
            Concept resultApprovalDate = conceptService.getConceptByUuid(Constants.RESULT_APPROVAL_DATE);
            Obs obs165462 = new Obs(person, resultApprovalDate, obsDatetime, location);
            obs165462.setValueDate(DateUtil.toDate(cd4.getLabResultDate()));
            encounter.addObs(obs165462);
        }

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

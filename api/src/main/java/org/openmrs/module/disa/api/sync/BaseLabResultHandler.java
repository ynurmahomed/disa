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
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.api.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Base class for LabResultHandlers.
 * </p>
 *
 * All LabResultHandlers should extend this class and override the handle
 * method.
 *
 * By default it does nothing with the lab result and passes it to the next
 * handler.
 *
 * A context object is available to handlers to share information between them.
 */
public abstract class BaseLabResultHandler implements LabResultHandler {

    public static final String ENCOUNTER_KEY = "ENCOUNTER";

    private static SyncContext context = new HashMapSyncContext();

    private LabResultHandler next;

    private EncounterService encounterService;

    private FormService formService;

    protected PersonService personService;

    protected ConceptService conceptService;

    @Autowired
    public final void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @Autowired
    public final void setFormService(FormService formService) {
        this.formService = formService;
    }

    @Autowired
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Autowired
    public final void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @Override
    public void setNext(LabResultHandler handler) {
        this.next = handler;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {
        if (next != null) {
            return next.handle(labResult);
        }
        return null;
    }

    /**
     * Builds the E-Lab encounter with common obs.
     *
     * @param patient   The patient
     * @param provider  The provider
     * @param location  The location
     * @param labResult The lab result
     * @return the E-Lab encounter.
     */
    protected Encounter getElabEncounter(Patient patient, Provider provider, Location location, LabResult labResult) {
        Encounter encounter = new Encounter();

        EncounterType encounterType = encounterService
                .getEncounterTypeByUuid(Constants.DISA_ENCOUNTER_TYPE);
        EncounterRole encounterRole = encounterService
                .getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);

        LocalDateTime authorisedDate = labResult.getLabResultDate().with(LocalTime.MIDNIGHT);
        encounter.setEncounterDatetime(DateUtil.toDate(authorisedDate));
        encounter.setPatient(patient);
        encounter.setEncounterType(encounterType);
        encounter.setLocation(location);
        encounter.setForm(formService.getFormByUuid(Constants.DISA_FORM));
        encounter.setProvider(encounterRole, provider);

        Person person = personService.getPersonByUuid(encounter.getPatient().getUuid());
        Date obsDatetime = encounter.getEncounterDatetime();

        // Order Number
        Concept orderId = conceptService.getConceptByUuid(Constants.ORDER_ID);
        Obs obs22771 = new Obs(person, orderId, obsDatetime, location);
        obs22771.setValueText(labResult.getRequestId().trim());
        encounter.addObs(obs22771);

        // Lab Number
        if (StringUtils.isNotEmpty(labResult.getHealthFacilityLabCode())) {
            Concept labNumber = conceptService.getConceptByUuid(Constants.LAB_NUMBER);
            Obs obs23835 = new Obs(person, labNumber, obsDatetime, location);
            obs23835.setValueText(labResult.getHealthFacilityLabCode());
            encounter.addObs(obs23835);
        }

        // Pregnant
        Concept pregnant = conceptService.getConceptByUuid(Constants.PREGNANT);
        Obs obs1982 = new Obs(person, pregnant, obsDatetime, location);
        if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.YES)
                || labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.GESTATION));
        } else if (labResult.getPregnant().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(labResult.getPregnant().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
        } else {
            obs1982.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
        }
        encounter.addObs(obs1982);

        // Breastfeeding
        Concept breastfeeding = conceptService.getConceptByUuid(Constants.LACTATION);
        Obs obs6332 = new Obs(person, breastfeeding, obsDatetime, location);
        if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
                || labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.SIM)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_YES));
        } else if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.NO)
                || StringUtils.stripAccents(labResult.getBreastFeeding().trim()).equalsIgnoreCase(Constants.NAO)) {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NO));
        } else {
            obs6332.setValueCoded(conceptService.getConceptByUuid(Constants.CONCEPT_NOT_FILLED));
        }
        encounter.addObs(obs6332);

        // Picking location
        if (StringUtils.isNotEmpty(labResult.getRequestingFacilityName())) {
            Concept pickingLocation = conceptService.getConceptByUuid(Constants.PICKING_LOCATION);
            Obs obs23883 = new Obs(person, pickingLocation, obsDatetime, location);
            obs23883.setValueText(labResult.getRequestingFacilityName());
            encounter.addObs(obs23883);
        }

        // Specimen collection Date
        if (labResult.getHarvestDate() != null) {
            Concept sampleCollectionDate = conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE);
            Obs obs23821 = new Obs(person, sampleCollectionDate, obsDatetime, location);
            obs23821.setValueDate(DateUtil.toDate(labResult.getHarvestDate()));
            encounter.addObs(obs23821);
        }

        // Sample regsitration date
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

        return encounter;
    }

    protected SyncContext getSyncContext() {
        return context;
    }

    protected void clearSyncContext() {
        context.clear();
    }
}

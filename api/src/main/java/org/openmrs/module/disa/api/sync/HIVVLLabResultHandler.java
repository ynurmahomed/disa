package org.openmrs.module.disa.api.sync;

import java.text.Normalizer;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
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

                createFsrObs(vl, encounter, dateWithLeadingZeros);

                labResult.setLabResultStatus(LabResultStatus.PROCESSED);
                getSyncContext().put(ENCOUNTER_KEY, encounter);
            }

        }

        return super.handle(labResult);
    }

    private void validateResult(HIVVLLabResult vl) {
        if (hasNoResult(vl)) {

            updateNotProcessed(vl, NotProcessingCause.NO_RESULT);

        } else if (!isNumeric(vl.getFinalResult().trim())
                && !(vl.getFinalResult().contains(Constants.LESS_THAN))
                && !(vl.getFinalResult().contains(Constants.MORE_THAN))
                && !(vl.getFinalResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL))) {

            updateNotProcessed(vl, NotProcessingCause.FLAGGED_FOR_REVIEW);

        } else if (vl.getFinalResult().contains(Constants.MORE_THAN)) {

            String trim = vl.getFinalResult().trim();
            String maybeNumeric = trim.substring(trim.indexOf(Constants.MORE_THAN) + 1).trim();
            if (!isNumeric(maybeNumeric)) {
                updateNotProcessed(vl, NotProcessingCause.FLAGGED_FOR_REVIEW);
            }
        }
    }

    private void createFsrObs(HIVVLLabResult labResult, Encounter encounter, Date dateWithLeadingZeros) {
        Obs obs23835 = new Obs();
        obs23835.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23835.setObsDatetime(dateWithLeadingZeros);
        obs23835.setConcept(conceptService.getConceptByUuid(Constants.LAB_NUMBER));
        obs23835.setLocation(encounter.getLocation());
        obs23835.setEncounter(encounter);
        if (StringUtils.isNotEmpty(labResult.getHealthFacilityLabCode())) {// RequestingFacilityCode
            obs23835.setValueText(labResult.getHealthFacilityLabCode());
            encounter.addObs(obs23835);
        }

        Obs obs23883 = new Obs();
        obs23883.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23883.setObsDatetime(dateWithLeadingZeros);
        obs23883.setConcept(conceptService.getConceptByUuid(Constants.PICKING_LOCATION));
        obs23883.setLocation(encounter.getLocation());
        obs23883.setEncounter(encounter);
        if (StringUtils.isNotEmpty(labResult.getRequestingFacilityName())) {// RequestingFacilityName
            obs23883.setValueText(labResult.getRequestingFacilityName());
            encounter.addObs(obs23883);
        }

        Obs obs23836 = new Obs();
        obs23836.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23836.setObsDatetime(dateWithLeadingZeros);
        obs23836.setConcept(conceptService.getConceptByUuid(Constants.ENCOUNTER_SERVICE));
        obs23836.setLocation(encounter.getLocation());
        obs23836.setEncounter(encounter);
        if (!(labResult.getEncounter() == null)
                && StringUtils.isNotEmpty(wardSelection(labResult.getEncounter().trim()))) {// WARD
            obs23836.setValueCoded(conceptService
                    .getConceptByName(wardSelection(labResult.getEncounter().trim())));
            encounter.addObs(obs23836);
        }

        Obs obs1982 = new Obs();
        obs1982.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs1982.setObsDatetime(dateWithLeadingZeros);
        obs1982.setConcept(conceptService.getConceptByUuid(Constants.PREGNANT));
        obs1982.setLocation(encounter.getLocation());
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

        Obs obs6332 = new Obs();
        obs6332.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs6332.setObsDatetime(dateWithLeadingZeros);
        obs6332.setConcept(conceptService.getConceptByUuid(Constants.LACTATION));
        obs6332.setLocation(encounter.getLocation());
        obs6332.setEncounter(encounter);
        if (labResult.getBreastFeeding().trim().equalsIgnoreCase(Constants.YES)
                || labResult.getPregnant().trim().equalsIgnoreCase(Constants.SIM)) {// BreastFeeding
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

        Obs obs23818 = new Obs();
        obs23818.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23818.setObsDatetime(dateWithLeadingZeros);
        obs23818.setConcept(conceptService.getConceptByUuid(Constants.REASON_FOR_TEST));
        obs23818.setLocation(encounter.getLocation());
        obs23818.setEncounter(encounter);
        if (labResult.getReasonForTest().trim().equalsIgnoreCase(Constants.ROUTINE)) {// ReasonForTest
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

        Obs obs23821 = new Obs();
        obs23821.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23821.setObsDatetime(dateWithLeadingZeros);
        obs23821.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_DATE));
        obs23821.setLocation(encounter.getLocation());
        obs23821.setEncounter(encounter);
        if (labResult.getHarvestDate() != null) {// SpecimenDatetime
            obs23821.setValueDate(DateUtil.toDate(labResult.getHarvestDate()));
            encounter.addObs(obs23821);
        }

        Obs obs23824 = new Obs();
        obs23824.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23824.setObsDatetime(dateWithLeadingZeros);
        obs23824.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_COLLECTION_METHOD));
        obs23824.setLocation(encounter.getLocation());
        obs23824.setEncounter(encounter);
        if (!(labResult.getHarvestType() == null)) {
            if (removeAccents(labResult.getHarvestType().trim()).equalsIgnoreCase(Constants.PV)) {// TypeOfSampleCollection
                obs23824
                        .setValueCoded(conceptService.getConceptByUuid(Constants.VENOUS_PUNCTURE));
                encounter.addObs(obs23824);
            } else if (removeAccents(labResult.getHarvestType().trim()).equalsIgnoreCase(Constants.PD)) {
                obs23824.setValueCoded(
                        conceptService.getConceptByUuid(Constants.DIGITAL_PUNCTURE));
                encounter.addObs(obs23824);
            }
        }

        Obs obs23826 = new Obs();
        obs23826.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23826.setObsDatetime(dateWithLeadingZeros);
        obs23826.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_DATE_RECEIPT));
        obs23826.setLocation(encounter.getLocation());
        obs23826.setEncounter(encounter);
        if (labResult.getDateOfSampleReceive() != null) {// ReceivedDateTime
            obs23826.setValueDate(DateUtil.toDate(labResult.getDateOfSampleReceive()));
            encounter.addObs(obs23826);
        }

        Obs obs23833 = new Obs();
        obs23833.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23833.setObsDatetime(dateWithLeadingZeros);
        obs23833.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_PROCESSING_DATE));
        obs23833.setLocation(encounter.getLocation());
        obs23833.setEncounter(encounter);
        if (labResult.getProcessingDate() != null) {// AnalysisDateTime
            obs23833.setValueDate(DateUtil.parseAtMidnight(labResult.getProcessingDate()));
            encounter.addObs(obs23833);
        }

        // LastViralLoadResult & LastViralLoadDate
        Obs obs165314 = new Obs();
        obs165314.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs165314.setObsDatetime(dateWithLeadingZeros);
        obs165314.setConcept(conceptService.getConceptByUuid(Constants.LAST_VIRALLOAD_RESULT));
        obs165314.setLocation(encounter.getLocation());
        obs165314.setEncounter(encounter);
        obs165314.setValueText(labResult.getLastViralLoadResult());
        if (DateUtil.isValidDate(labResult.getLastViralLoadDate())) {

            try {
                obs165314.setObsDatetime((DateUtil.stringToDate(labResult.getLastViralLoadDate())));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            encounter.addObs(obs165314);
        }

        // PrimeiraLinha & SegundaLinha
        Obs obs21151 = new Obs();
        obs21151.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs21151.setObsDatetime(dateWithLeadingZeros);
        obs21151.setConcept(conceptService.getConceptByUuid(Constants.LINHA_TERAPEUTICA));
        obs21151.setLocation(encounter.getLocation());
        obs21151.setEncounter(encounter);
        if (labResult.getPrimeiraLinha().equalsIgnoreCase(Constants.SIM)) {
            obs21151.setValueCoded(conceptService.getConceptByUuid(Constants.PRIMEIRA_LINHA));
            encounter.addObs(obs21151);
        } else if (labResult.getSegundaLinha().equalsIgnoreCase(Constants.SIM)) {
            obs21151.setValueCoded(conceptService.getConceptByUuid(Constants.SEGUNDA_LINHA));
            encounter.addObs(obs21151);
        }

        // ARTRegimen
        Obs obs165315 = new Obs();
        obs165315.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs165315.setObsDatetime(dateWithLeadingZeros);
        obs165315.setConcept(conceptService.getConceptByUuid(Constants.ART_REGIMEN));
        obs165315.setLocation(encounter.getLocation());
        obs165315.setEncounter(encounter);
        if (StringUtils.isNotEmpty(labResult.getArtRegimen())) {
            obs165315.setValueText(labResult.getArtRegimen());
            encounter.addObs(obs165315);
        }

        // DataDeInicioDoTARV
        Obs obs1190 = new Obs();
        obs1190.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs1190.setObsDatetime(dateWithLeadingZeros);
        obs1190.setConcept(conceptService.getConceptByUuid(Constants.ART_START_DATE));
        obs1190.setLocation(encounter.getLocation());
        obs1190.setEncounter(encounter);
        if (DateUtil.isValidDate(labResult.getDataDeInicioDoTARV())) {
            try {
                obs1190.setValueDatetime(DateUtil.stringToDate(labResult.getDataDeInicioDoTARV()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            encounter.addObs(obs1190);
        }

        // SpecimenDatetime
        Obs obs23840 = new Obs();
        obs23840.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23840.setObsDatetime(dateWithLeadingZeros);
        obs23840.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_REQUEST_DATE));
        obs23840.setLocation(encounter.getLocation());
        obs23840.setEncounter(encounter);
        if (labResult.getHarvestDate() != null) {
            obs23840.setValueDate(DateUtil.toDate(labResult.getHarvestDate()));
            encounter.addObs(obs23840);
        }

        // LIMSSpecimenSourceCode
        Obs obs23832 = new Obs();
        obs23832.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23832.setObsDatetime(dateWithLeadingZeros);
        obs23832.setConcept(conceptService.getConceptByUuid(Constants.SAMPLE_TYPE));
        obs23832.setLocation(encounter.getLocation());
        obs23832.setEncounter(encounter);
        if (!(labResult.getSampleType() == null)
                && labResult.getSampleType().trim().equalsIgnoreCase(Constants.DRYBLOODSPOT)) {
            obs23832.setValueCoded(conceptService.getConceptByUuid(Constants.DRY_BLOOD_SPOT));
            encounter.addObs(obs23832);
        } else if (!(labResult.getSampleType() == null)
                && labResult.getSampleType().trim().equalsIgnoreCase(Constants.PLASMA)) {
            obs23832.setValueCoded(conceptService.getConceptByUuid(Constants.PLASMA_));
            encounter.addObs(obs23832);
        }

        // Using FinalViralLoadResult column only
        // field: Cópias/ml
        if (isNumeric(labResult.getFinalResult().trim())) {
            Obs obs856 = new Obs();
            obs856.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
            obs856.setObsDatetime(dateWithLeadingZeros);
            obs856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
            obs856.setLocation(encounter.getLocation());
            obs856.setEncounter(encounter);
            obs856.setValueNumeric(Double.valueOf(labResult.getFinalResult().trim()));
            encounter.addObs(obs856);
        } else

        // field: dropbox with answer label Indectetável
        if (labResult.getFinalResult().trim().equalsIgnoreCase(Constants.INDETECTAVEL)) {
            Obs obs1305 = new Obs();
            obs1305.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
            obs1305.setObsDatetime(dateWithLeadingZeros);
            obs1305.setConcept(
                    conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
            obs1305.setValueCoded(
                    conceptService.getConceptByUuid(Constants.UNDETECTABLE_VIRAL_LOAD));
            obs1305.setLocation(encounter.getLocation());
            obs1305.setEncounter(encounter);
            encounter.addObs(obs1305);
        } else

        // field: dropbox with answer <
        if (labResult.getFinalResult().contains(Constants.LESS_THAN)) {
            Obs obs1305 = new Obs();
            obs1305.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
            obs1305.setObsDatetime(dateWithLeadingZeros);
            obs1305.setConcept(
                    conceptService.getConceptByUuid(Constants.HIV_VIRAL_LOAD_QUALITATIVE));
            obs1305.setValueCoded(conceptService.getConceptByUuid(Constants.LESSTHAN));
            obs1305.setLocation(encounter.getLocation());
            obs1305.setEncounter(encounter);
            obs1305.setComment(labResult.getFinalResult()
                    .trim()
                    .substring(1)
                    .replace(Constants.LESS_THAN, "")
                    .replace(Constants.COPIES, "")
                    .replace(Constants.FORWARD_SLASH, "")
                    .replace(Constants.ML, "")
                    .trim());
            encounter.addObs(obs1305);
        } else if (labResult.getFinalResult().contains(Constants.MORE_THAN)) {
            Obs obs856 = new Obs();
            obs856.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
            obs856.setObsDatetime(dateWithLeadingZeros);
            obs856.setConcept(conceptService.getConceptByUuid(Constants.VIRAL_LOAD_COPIES));
            obs856.setLocation(encounter.getLocation());
            obs856.setEncounter(encounter);
            String trim = labResult.getFinalResult().trim();
            String numericPart = trim.substring(trim.indexOf(Constants.MORE_THAN) + 1);
            obs856.setValueNumeric(Double.valueOf(numericPart));
            encounter.addObs(obs856);
        }

        Obs obs23839 = new Obs();
        obs23839.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23839.setObsDatetime(dateWithLeadingZeros);
        obs23839.setConcept(conceptService.getConceptByUuid(Constants.APPROVED_BY));
        obs23839.setEncounter(encounter);
        obs23839.setLocation(encounter.getLocation());
        if (!(labResult.getAprovedBy() == null) && StringUtils.isNotEmpty(labResult.getAprovedBy().trim())) {
            obs23839.setValueText(labResult.getAprovedBy().trim());
            encounter.addObs(obs23839);
        }

        Obs obs23841 = new Obs();
        obs23841.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs23841.setObsDatetime(dateWithLeadingZeros);
        obs23841.setConcept(conceptService.getConceptByUuid(Constants.LAB_COMMENTS));
        obs23841.setLocation(encounter.getLocation());
        obs23841.setEncounter(encounter);
        if (!(labResult.getLabComments() == null) && StringUtils.isNotEmpty(labResult.getLabComments().trim())) {
            obs23841.setValueText(labResult.getLabComments().trim());
            encounter.addObs(obs23841);
        }

        Obs obs22771 = new Obs();
        obs22771.setPerson(personService.getPersonByUuid(encounter.getPatient().getUuid()));
        obs22771.setObsDatetime(dateWithLeadingZeros);
        obs22771.setConcept(conceptService.getConceptByUuid(Constants.ORDER_ID));
        obs22771.setLocation(encounter.getLocation());
        obs22771.setEncounter(encounter);
        if (!(labResult.getRequestId() == null) && StringUtils.isNotEmpty(labResult.getRequestId().trim())) {
            obs22771.setValueText(labResult.getRequestId().trim());
            encounter.addObs(obs22771);
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

    private String wardSelection(String we) {
        String wardConcept;
        switch (we) {
            case "CI":
                wardConcept = Constants.CONSULTA_INTEGRADA;
                break;
            case "SMI":
                wardConcept = Constants.SAUDE_MATERNO_INFANTIL;
                break;
            case "CPN":
                wardConcept = Constants.CONSULTA_PRE_NATAL;
                break;
            case "HDD":
                wardConcept = Constants.HOSPITAL_DO_DIA;
                break;
            case "CCR":
                wardConcept = Constants.CONSULTA_DE_CRIANCAS_EM_RISCO;
                break;
            case "TARV":
                wardConcept = Constants.TARV;
                break;
            case "TAP":
                wardConcept = Constants.TRIAGEM_PEDIATRIA;
                break;
            case "TAD":
                wardConcept = Constants.TRIAGEM_ADULTOS;
                break;
            case "PED":
                wardConcept = Constants.ENF_PEDIATRIA;
                break;
            case "LAB":
                wardConcept = Constants.LABORATORIO;
                break;
            default:
                wardConcept = Constants.OUTRO_NAO_CODIFICADO;
        }
        return wardConcept;
    }

    public String removeAccents(String specialCharacter) {
        try {
            byte[] bs = specialCharacter.getBytes("ISO-8859-15");
            return Normalizer.normalize(new String(bs, "UTF-8"), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

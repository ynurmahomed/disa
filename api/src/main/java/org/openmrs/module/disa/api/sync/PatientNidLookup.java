package org.openmrs.module.disa.api.sync;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;
import org.openmrs.module.disa.NotProcessingCause;
import org.openmrs.module.disa.api.DisaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Handler for lab results with patient NID.
 * </p>
 *
 * Checks if the nid exists and adds the corresponding patient to the sync
 * context.
 *
 * If the NID does not exist, or if it is duplicated the lab result is marked as
 * NOT_PROCESSED with the cause NID_NOT_FOUND.
 */
@Component
public class PatientNidLookup extends BaseLabResultHandler {

    private static Logger logger = LoggerFactory.getLogger(PatientNidLookup.class);

    public static final String PATIENT_KEY = "PATIENT";

    private DisaService disaService;

    @Autowired
    public PatientNidLookup(DisaService disaService) {
        this.disaService = disaService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {

        if (labResult.isPending()) {
            if (labResult.getNid() == null) {
                labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
                labResult.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
                logger.debug(PATIENT_KEY + " not found. NID is not present.");
            } else {
                lookupPatient(labResult);
            }
        }

        return super.handle(labResult);
    }

    private void lookupPatient(LabResult labResult) {
        String nid = labResult.getNid().trim();
        List<Integer> patientIds = disaService.getPatientByNid(nid);
        if (patientIds.isEmpty()) {
            labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
            labResult.setNotProcessingCause(NotProcessingCause.NID_NOT_FOUND);
            logger.debug(PATIENT_KEY + " not found for nid {}", labResult.getNid());
        } else if (patientIds.size() > 1) {
            // notify duplication
            logger.info("Os pacientes do OpenMRS com os Ids: {} partilham o mesmo NID: {}", patientIds, nid);
            labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
            labResult.setNotProcessingCause(NotProcessingCause.DUPLICATE_NID);
        } else {
            List<Patient> patients = disaService.getPatientByPatientId(patientIds.get(0));
            getSyncContext().put(PATIENT_KEY, patients.get(0));
        }
    }
}

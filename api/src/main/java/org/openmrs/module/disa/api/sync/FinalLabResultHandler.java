package org.openmrs.module.disa.api.sync;

import org.openmrs.Encounter;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Final step of the lab result handler chain.
 * </p>
 *
 * Results that have status NOT_PROCESSED are updated in the integration server.
 *
 * For results that have status PROCESSED, a fsr log is created and then the
 * result is updated in the integration server.
 */
@Component
public class FinalLabResultHandler extends BaseLabResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(FinalLabResultHandler.class);

    private LabResultService labResultService;
    private DisaService disaService;

    @Autowired
    public FinalLabResultHandler(LabResultService labResultService, DisaService disaService) {
        this.labResultService = labResultService;
        this.disaService = disaService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {
        if (labResult.isNotProcessed()) {
            handleNotProcessed(labResult);
        } else if (labResult.isProcessed()) {
            handleProcessed(labResult);
        } else {
            logger.debug("LabResultStatus is neither PROCESSED or NOT_PROCESSED, skipping");
        }
        // Terminate the chain
        return labResult.getLabResultStatus();
    }

    private void handleNotProcessed(LabResult labResult) {
        labResultService.updateLabResult(labResult);
    }

    private void handleProcessed(LabResult labResult) {
        Encounter encounter = (Encounter) getSyncContext().get(ENCOUNTER_KEY);
        if (encounter == null) {
            throw new DisaModuleAPIException(ENCOUNTER_KEY + " is missing from the sync context");
        }

        disaService.handleProcessedLabResult(labResult, encounter);

        // Clear the sync context
        clearSyncContext();

    }
}

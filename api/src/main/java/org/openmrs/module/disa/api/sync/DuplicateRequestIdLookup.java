package org.openmrs.module.disa.api.sync;

import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;
import org.openmrs.module.disa.NotProcessingCause;
import org.openmrs.module.disa.api.DisaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Handler for lab results with duplicated request id.
 * </p>
 *
 * Checks if there is a lab result with the same request id in the FsrLog.
 */
@Component
public class DuplicateRequestIdLookup extends BaseLabResultHandler {

    private DisaService disaService;

    @Autowired
    public DuplicateRequestIdLookup(DisaService disaService) {
        this.disaService = disaService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {

        if (labResult.isPending() && disaService.existsInFsrLog(labResult)) {
            labResult.setLabResultStatus(LabResultStatus.NOT_PROCESSED);
            labResult.setNotProcessingCause(NotProcessingCause.DUPLICATED_REQUEST_ID);
        }

        return super.handle(labResult);
    }
}

package org.openmrs.module.disa.api.sync;

import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;

public class LabResultProcessor {
    private LabResultHandler resultHandlerChain;
    private int processed = 0;
    private int notProcessed = 0;

    public LabResultProcessor(LabResultHandler resultHandlerChain) {
        this.resultHandlerChain = resultHandlerChain;
    }

    public void processResult(LabResult labResult) {
        LabResultStatus status = resultHandlerChain.handle(labResult);
        if (status == LabResultStatus.PROCESSED) {
            processed++;
        } else if (status == LabResultStatus.NOT_PROCESSED) {
            notProcessed++;
        }
    }

    public int getProcessedCount() {
        return processed;
    }

    public int getNotProcessedCount() {
        return notProcessed;
    }
}

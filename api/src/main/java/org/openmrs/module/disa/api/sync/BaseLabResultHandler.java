package org.openmrs.module.disa.api.sync;

import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;

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

    protected SyncContext getSyncContext() {
        return context;
    }

    protected void clearSyncContext() {
        context.clear();
    }
}

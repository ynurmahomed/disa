package org.openmrs.module.disa.api.exception;

import org.openmrs.api.APIException;

/**
 * General module API exception class.
 */
public class DisaModuleAPIException extends APIException {

    public DisaModuleAPIException() {
    }

    public DisaModuleAPIException(String message) {
        super(message);
    }

    public DisaModuleAPIException(Throwable cause) {
        super(cause);
    }

    public DisaModuleAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisaModuleAPIException(String messageKey, Object[] parameters) {
        super(messageKey, parameters);
    }

    public DisaModuleAPIException(String messageKey, Object[] parameters, Throwable cause) {
        super(messageKey, parameters, cause);
    }
}

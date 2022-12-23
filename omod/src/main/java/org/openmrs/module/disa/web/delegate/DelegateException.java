package org.openmrs.module.disa.web.delegate;

public class DelegateException extends Exception {
    public DelegateException(String message) {
        super(message);
    }

    public DelegateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DelegateException(Throwable cause) {
        super(cause);
    }
}

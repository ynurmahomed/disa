package org.openmrs.module.disa.api.config;

public class DisaUserAgentHolder {
    private final String userAgent;

    public DisaUserAgentHolder(String userAgent) {
        this.userAgent = userAgent;
    }

    public String get() {
        return userAgent;
    }
}

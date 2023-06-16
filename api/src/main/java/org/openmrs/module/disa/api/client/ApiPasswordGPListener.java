package org.openmrs.module.disa.api.client;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class ApiPasswordGPListener implements GlobalPropertyListener {

    private DisaAPIHttpClient disaAPIHttpClient;

    @Autowired
    public ApiPasswordGPListener(DisaAPIHttpClient disaAPIHttpClient) {
        this.disaAPIHttpClient = disaAPIHttpClient;
    }

    @Override
    public boolean supportsPropertyName(String propertyName) {
        return Constants.DISA_PASSWORD.equals(propertyName);
    }

    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        String password = newValue.getPropertyValue();
        disaAPIHttpClient.setPassword(password != null ? password : "");
    }

    @Override
    public void globalPropertyDeleted(String propertyName) {
        disaAPIHttpClient.setPassword("");
    }
}

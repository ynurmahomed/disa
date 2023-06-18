package org.openmrs.module.disa.api.client;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class ApiUsernameGPListener implements GlobalPropertyListener {

    private DisaAPIHttpClient disaAPIHttpClient;

    @Autowired
    public ApiUsernameGPListener(DisaAPIHttpClient disaAPIHttpClient) {
        this.disaAPIHttpClient = disaAPIHttpClient;
    }

    @Override
    public boolean supportsPropertyName(String propertyName) {
        return Constants.DISA_USERNAME.equals(propertyName);
    }

    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        String username = newValue.getPropertyValue();
        disaAPIHttpClient.setUsername(username != null ? username : "");
    }

    @Override
    public void globalPropertyDeleted(String propertyName) {
        disaAPIHttpClient.setUsername("");
    }

}
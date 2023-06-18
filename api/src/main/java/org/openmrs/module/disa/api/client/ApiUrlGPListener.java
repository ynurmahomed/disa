package org.openmrs.module.disa.api.client;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class ApiUrlGPListener implements GlobalPropertyListener {

    private DisaAPIHttpClient disaAPIHttpClient;

    @Autowired
    public ApiUrlGPListener(DisaAPIHttpClient disaAPIHttpClient) {
        this.disaAPIHttpClient = disaAPIHttpClient;
    }

    @Override
    public boolean supportsPropertyName(String propertyName) {
        return Constants.DISA_URL.equals(propertyName);
    }

    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        String url = newValue.getPropertyValue();
        disaAPIHttpClient.setURLBase(url != null ? url : "");
    }

    @Override
    public void globalPropertyDeleted(String propertyName) {
        disaAPIHttpClient.setURLBase("");
    }
}

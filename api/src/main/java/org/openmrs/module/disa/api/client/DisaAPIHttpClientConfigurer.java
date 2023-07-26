package org.openmrs.module.disa.api.client;

import java.util.Arrays;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;

public class DisaAPIHttpClientConfigurer implements GlobalPropertyListener {

    private List<String> properties = Arrays.asList(Constants.DISA_URL, Constants.DISA_USERNAME,
            Constants.DISA_PASSWORD);

    private DisaAPIHttpClient disaAPIHttpClient;

    @Autowired
    public DisaAPIHttpClientConfigurer(DisaAPIHttpClient disaAPIHttpClient) {
        this.disaAPIHttpClient = disaAPIHttpClient;
    }

    @Override
    public boolean supportsPropertyName(String propertyName) {
        return properties.contains(propertyName);
    }

    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        String property = newValue.getProperty();
        String value = newValue.getPropertyValue() != null ? newValue.getPropertyValue() : "";

        switch (property) {
            case Constants.DISA_URL:
                disaAPIHttpClient.setURLBase(value);
                break;
            case Constants.DISA_USERNAME:
                disaAPIHttpClient.setUsername(value);
                break;
            case Constants.DISA_PASSWORD:
                disaAPIHttpClient.setPassword(value);
                break;
            default:
                break;
        }
    }

    @Override
    public void globalPropertyDeleted(String propertyName) {
        switch (propertyName) {
            case Constants.DISA_URL:
                disaAPIHttpClient.setURLBase("");
                break;
            case Constants.DISA_USERNAME:
                disaAPIHttpClient.setUsername("");
                break;
            case Constants.DISA_PASSWORD:
                disaAPIHttpClient.setPassword("");
                break;
            default:
                break;
        }
    }

}

package org.openmrs.module.disa.web.delegate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.web.client.OrgUnitDisaHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class OrgUnitDelegate {

    private static final Logger log = LoggerFactory.getLogger(OrgUnitDelegate.class);

    @Autowired
    private OrgUnitDisaHttpClient client;

    public OrgUnit getOrgUnit(String code) throws DelegateException {

        try {
            String response = client.getOrgUnit(code);
            return new Gson().fromJson(response, OrgUnit.class);
        } catch (IOException | URISyntaxException e) {
            String message = e.getMessage();
            if (e instanceof HttpResponseException) {
                message = "" + ((HttpResponseException) e).getStatusCode();
            }
            log.error("Error fetching org unit {}: {}", code, message);
            e.printStackTrace();
            throw new DelegateException("Unexpected error.");
        }
    }

    public List<OrgUnit> search(String term) throws DelegateException {

        try {
            String response = client.searchOrgUnits(term);
            TypeToken<List<OrgUnit>> listType = new TypeToken<List<OrgUnit>>() {
            };
            return new Gson().fromJson(response, listType.getType());
        } catch (IOException | URISyntaxException e) {
            String message = e.getMessage();
            if (e instanceof HttpResponseException) {
                message = "" + ((HttpResponseException) e).getStatusCode();
            }
            log.error("Error searching org units: {}", message);
            e.printStackTrace();
            throw new DelegateException("Unexpected error.");
        }
    }
}

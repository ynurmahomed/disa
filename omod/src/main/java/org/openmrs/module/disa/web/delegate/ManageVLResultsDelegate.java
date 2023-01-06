package org.openmrs.module.disa.web.delegate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.web.client.DisaAPIHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class ManageVLResultsDelegate {

    private static final Logger log = LoggerFactory.getLogger(ManageVLResultsDelegate.class);

    @Autowired
    private DisaAPIHttpClient client;

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

    public List<OrgUnit> searchOrgUnits(String term) throws DelegateException {

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

    @Authorized({"Gerir resultados no Disa Interoperabilidade"})
    public Disa getViralLoad(String requestId) throws DelegateException {
		try {
			log.info("Fetching Lab Result {}", requestId);
			String response = client.getViralLoad(requestId);
			return new Gson().fromJson(response, Disa.class);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error fetching Lab Result: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

    @Authorized({"Gerir resultados no Disa Interoperabilidade"})
    public void deleteViralLoad(String requestId) throws DelegateException {
		try {
			log.info("Deleting Lab Result {}", requestId);
			client.deleteViralLoad(requestId);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error processing delete: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

    @Authorized({"Gerir resultados no Disa Interoperabilidade"})
	public Disa updateViralLoad(String requestId, Disa updates) throws DelegateException {
		try {
			log.info("Updating Lab Result {}", requestId);
			String response = client.updateViralLoad(requestId, new Gson().toJson(updates));
			return new Gson().fromJson(response, Disa.class);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error processing update: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}
}

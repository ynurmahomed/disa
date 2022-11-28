package org.openmrs.module.disa.web.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.disa.extension.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * A HTTP client for the OrgUnit resource from DISA API.
 */
@Component
public class DisaAPIHttpClient {

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private String username;
    private String password;
    private String URLBase;


    @PostConstruct
	public void postConstruct() {
		setURLBase(administrationService.getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		setUsername(administrationService.getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		setPassword(administrationService.getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
	}

    public String searchOrgUnits(String term) throws IOException, URISyntaxException {
        URI url;

        url = new URIBuilder(URLBase)
                .setPath("/services/orgunits/search")
                .addParameter("term", term)
                .build();

        Executor executor = Executor.newInstance()
                .auth(username, password);

        Request request = Request.Get(url);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        return executor.execute(request)
                .handleResponse(responseHandler);
    }

    public String getOrgUnit(String code) throws URISyntaxException, IOException {
        URI url;

        url = new URIBuilder(URLBase)
                .setPathSegments("services", "orgunits", code)
                .build();

        Executor executor = Executor.newInstance()
                .auth(username, password);

        Request request = Request.Get(url);

        ResponseHandler<String> responseHandler = new BasicResponseHandler();

        return executor.execute(request)
                .handleResponse(responseHandler);
    }

    public String getViralLoad(String requestId) throws URISyntaxException, IOException {
		URI url;

		url = new URIBuilder(URLBase)
				.setPathSegments("services", "viralloads", requestId)
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		return executor.execute(request)
				.handleResponse(responseHandler);
	}

    public void deleteViralLoad(String requestId) throws IOException, URISyntaxException {
		URI url;

		url = new URIBuilder(URLBase)
				.setPath(String.format("/services/viralloads/%s", requestId))
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		HttpResponse response = executor.execute(Request.Delete(url))
				.returnResponse();

		StatusLine status = response.getStatusLine();

		if (status.getStatusCode() != 200) {
			throw new HttpResponseException(
					status.getStatusCode(),
					status.getReasonPhrase());
		}

	}

	public String updateViralLoad(String requestId, String payload) throws IOException, URISyntaxException {
		URI url;

		url = new URIBuilder(URLBase)
				.setPath(String.format("/services/viralloads/%s", requestId))
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Patch(url)
				.bodyString(payload, ContentType.APPLICATION_JSON);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		return executor.execute(request)
				.handleResponse(responseHandler);
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getURLBase() {
        return URLBase;
    }

    public void setURLBase(String uRLBase) {
        URLBase = uRLBase;
    }
}

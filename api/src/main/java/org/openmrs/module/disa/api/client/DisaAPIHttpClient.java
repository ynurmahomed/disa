package org.openmrs.module.disa.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A HTTP client for the OrgUnit resource from DISA API.
 */
@Component
public class DisaAPIHttpClient {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private AdministrationService administrationService;
	private Gson gson;
	private boolean isSetUp;
	private String username;
	private String password;
	private String URLBase;

	@Autowired
	public DisaAPIHttpClient(
			@Qualifier("adminService") AdministrationService administrationService,
			Gson gson) {
		this.administrationService = administrationService;
		this.gson = gson;
	}

	public Page<Disa> searchLabResults(
			LocalDateTime startDate,
			LocalDateTime endDate,
			String requestId,
			String referringRequestID,
			String viralLoadStatus,
			String notProcessingCause,
			String nid,
			List<String> healthFacilityLabCodes,
			int pageNumber,
			int pageSize,
			String orderBy,
			String direction) throws URISyntaxException, IOException {

		setUp();

		URIBuilder builder = new URIBuilder(URLBase)
				.setPathSegments("services", "viralloads", "search-form")
				.addParameter("startDate", startDate.format(DATE_TIME_FORMATTER))
				.addParameter("endDate", endDate.format(DATE_TIME_FORMATTER))
				.addParameter("requestId", requestId)
				.addParameter("referringRequestID", referringRequestID)
				.addParameter("viralLoadStatus", viralLoadStatus)
				.addParameter("notProcessingCause", notProcessingCause)
				.addParameter("nid", nid)
				.addParameter("pageNumber", String.valueOf(pageNumber))
				.addParameter("pageSize", String.valueOf(pageSize))
				.addParameter("orderBy", orderBy)
				.addParameter("direction", direction);

		for (String code : healthFacilityLabCodes) {
			builder.addParameter("healthFacilityLabCode", code);
		}

		URI url = builder.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		TypeToken<Page<Disa>> pageType = new TypeToken<Page<Disa>>() {};

		return gson.fromJson(jsonResponse, pageType.getType());
	}

	public List<OrgUnit> searchOrgUnits(String term) throws URISyntaxException, IOException {
		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "orgunits", "search")
				.addParameter("term", term)
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		TypeToken<List<OrgUnit>> listType = new TypeToken<List<OrgUnit>>() {
		};
		return gson.fromJson(jsonResponse, listType.getType());

	}

	public OrgUnit getOrgUnitByCode(String code) throws URISyntaxException, IOException {
		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "orgunits", code)
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		return gson.fromJson(jsonResponse, OrgUnit.class);

	}

	public Disa getResultByRequestId(String requestId) throws URISyntaxException, IOException {
		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "viralloads", requestId)
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		return gson.fromJson(jsonResponse, Disa.class);
	}

	public void deleteResultByRequestId(String requestId) throws IOException, URISyntaxException {
		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "viralloads", requestId)
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

	public String updateResult(Disa labResult) throws IOException, URISyntaxException {
		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "viralloads", labResult.getRequestId())
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Patch(url)
				.bodyString(gson.toJson(labResult), ContentType.APPLICATION_JSON);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		return executor.execute(request)
				.handleResponse(responseHandler);
	}

	private void setUp() {
		if (!isSetUp) {
			URLBase = administrationService.getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue();
			username = administrationService.getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue();
			password = administrationService.getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue();
			isSetUp = true;
		}
	}
}

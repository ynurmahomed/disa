package org.openmrs.module.disa.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.TypeOfResult;
import org.openmrs.module.disa.api.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A HTTP client for the OrgUnit resource from DISA API.
 */
@Component
public class DisaAPIHttpClient {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private Gson gson;
	private String username;
	private String password;
	private String URLBase;

	@Autowired
	public DisaAPIHttpClient(Gson gson) {
		this.gson = gson;
	}

	public Page<LabResult> searchLabResults(
			LocalDateTime startDate,
			LocalDateTime endDate,
			String requestId,
			String labResultStatus,
			String notProcessingCause,
			TypeOfResult typeOfResult,
			String nid,
			List<String> healthFacilityLabCodes,
			String search,
			int pageNumber,
			int pageSize,
			String orderBy,
			String direction) throws URISyntaxException, IOException {

		URIBuilder builder = new URIBuilder(URLBase)
				.setPathSegments("services", "v2", "viralloads", "search-form")
				.addParameter("requestId", requestId)
				.addParameter("labResultStatus", labResultStatus)
				.addParameter("notProcessingCause", notProcessingCause)
				.addParameter("typeOfResult", typeOfResult == TypeOfResult.ALL ? "" : typeOfResult.name())
				.addParameter("nid", nid)
				.addParameter("pageNumber", String.valueOf(pageNumber))
				.addParameter("pageSize", String.valueOf(pageSize))
				.addParameter("orderBy", orderBy)
				.addParameter("direction", direction)
				.addParameter("search", search);

		if (startDate != null) {
			builder.addParameter("startDate", startDate.format(DATE_TIME_FORMATTER));
		}

		if (endDate != null) {
			builder.addParameter("endDate", endDate.format(DATE_TIME_FORMATTER));
		}

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

		TypeToken<Page<LabResult>> pageType = new TypeToken<Page<LabResult>>() {
		};

		return gson.fromJson(jsonResponse, pageType.getType());
	}

	public List<LabResult> getAllLabResults(
			LocalDateTime startDate,
			LocalDateTime endDate,
			String requestId,
			String labResultStatus,
			String notProcessingCause,
			String nid,
			List<String> healthFacilityLabCodes) throws URISyntaxException, IOException {

		URIBuilder builder = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", "export")
				.addParameter("requestId", requestId)
				.addParameter("labResultStatus", labResultStatus)
				.addParameter("notProcessingCause", notProcessingCause)
				.addParameter("nid", nid);

		if (startDate != null) {
			builder.addParameter("startDate", startDate.format(DATE_TIME_FORMATTER));
		}

		if (endDate != null) {
			builder.addParameter("endDate", endDate.format(DATE_TIME_FORMATTER));
		}

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

		TypeToken<List<LabResult>> pageType = new TypeToken<List<LabResult>>() {
		};

		return gson.fromJson(jsonResponse, pageType.getType());
	}

	public List<OrgUnit> searchOrgUnits(String term) throws URISyntaxException, IOException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "v2", "orgunits", "search")
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

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "v2", "orgunits", code)
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		return gson.fromJson(jsonResponse, OrgUnit.class);

	}

	public LabResult getResultById(long id) throws URISyntaxException, IOException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Get(url);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		String jsonResponse = executor.execute(request)
				.handleResponse(responseHandler);

		return gson.fromJson(jsonResponse, LabResult.class);
	}

	public void deleteResultById(long id) throws IOException, URISyntaxException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
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

	public String updateResult(LabResult labResult) throws IOException, URISyntaxException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(labResult.getId()))
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Patch(url)
				.bodyString(gson.toJson(labResult), ContentType.APPLICATION_JSON);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		return executor.execute(request)
				.handleResponse(responseHandler);
	}

	/**
	 * This method is used to find a HF code that is not authorised for the user
	 *
	 * @param healthFacilityLabCodes
	 * @return The first code that is not authorised. If all codes are authorised,
	 *         or if there is an error returns null.
	 * @throws URISyntaxException
	 */
	public String findUnauthorisedSismaCode(List<String> healthFacilityLabCodes) {

		String code = null;

		Executor executor = Executor.newInstance()
				.auth(username, password);

		try {
			Iterator<String> iterator = healthFacilityLabCodes.iterator();
			while (iterator.hasNext()) {
				code = iterator.next();
				URIBuilder builder = new URIBuilder(URLBase)
						.setPathSegments("services", "v2", "viralloads", "search-form")
						.addParameter("healthFacilityLabCode", code);
				Request request = Request.Head(builder.build());
				HttpResponse response = executor.execute(request).returnResponse();

				if (response.getStatusLine().getStatusCode() == 403) {
					return code;
				}
			}
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return null;
		}

		return code;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setURLBase(String uRLBase) {
		URLBase = uRLBase;
	}
}

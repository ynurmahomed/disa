package org.openmrs.module.disa.api.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.TypeOfResult;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A HTTP client for the OrgUnit resource from DISA API.
 */
@Component
public class DisaAPIHttpClient {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private AdministrationService administrationService;
	private ObjectMapper objectMapper;
	private boolean isSetUp;
	private String username;
	private String password;
	private String URLBase;

	private RestTemplate restTemplate;

	@Autowired
	public DisaAPIHttpClient(
			RestTemplate restTemplate,
			@Qualifier("adminService") AdministrationService administrationService,
			ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.administrationService = administrationService;
		this.objectMapper = objectMapper;
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
			String direction) throws URISyntaxException {

		setUp();

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

		ParameterizedTypeReference<Page<LabResult>> typeRef = new ParameterizedTypeReference<Page<LabResult>>() {
		};
		ResponseEntity<Page<LabResult>> response = restTemplate.exchange(builder.build(), HttpMethod.GET,
				new HttpEntity<>(getHeaders()),
				typeRef);

		return response.getBody();
	}

	public List<LabResult> getAllLabResults(
			LocalDateTime startDate,
			LocalDateTime endDate,
			String requestId,
			String labResultStatus,
			String notProcessingCause,
			String nid,
			List<String> healthFacilityLabCodes) throws URISyntaxException {

		setUp();

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

		ParameterizedTypeReference<List<LabResult>> typeRef = new ParameterizedTypeReference<List<LabResult>>() {
		};
		ResponseEntity<List<LabResult>> response = restTemplate.exchange(builder.build(), HttpMethod.GET,
				new HttpEntity<>(getHeaders()),
				typeRef);

		return response.getBody();
	}

	public List<OrgUnit> searchOrgUnits(String term) throws URISyntaxException {

		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "v2", "orgunits", "search")
				.addParameter("term", term)
				.build();

		ParameterizedTypeReference<List<OrgUnit>> typeRef = new ParameterizedTypeReference<List<OrgUnit>>() {
		};
		ResponseEntity<List<OrgUnit>> response = restTemplate.exchange(url, HttpMethod.GET,
				new HttpEntity<>(getHeaders()),
				typeRef);

		return response.getBody();

	}

	public OrgUnit getOrgUnitByCode(String code) throws URISyntaxException {

		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "v2", "orgunits", code)
				.build();

		ResponseEntity<OrgUnit> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()),
				OrgUnit.class);

		return response.getBody();

	}

	public LabResult getResultById(long id) throws URISyntaxException {

		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
				.build();

		ResponseEntity<LabResult> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()),
				LabResult.class);

		return response.getBody();

	}

	public void deleteResultById(long id) throws IOException, URISyntaxException {

		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
				.build();

		restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), String.class);

	}

	public String updateResult(LabResult labResult) throws IOException, URISyntaxException {

		setUp();

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(labResult.getId()))
				.build();

		// We can't configure RestTemplate to use HttpComponentsClientHttpRequestFactory
		// to support PATCH requests due to spring-web and httpcomponents being loaded
		// by different classloaders in OpenMRS. So we'll use httpcomponents directly.
		Executor executor = Executor.newInstance()
				.auth(username, password);

		Request request = Request.Patch(url)
				.bodyString(objectMapper.writeValueAsString(labResult), ContentType.APPLICATION_JSON);

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

		setUp();

		String code = null;

		try {
			Iterator<String> iterator = healthFacilityLabCodes.iterator();
			while (iterator.hasNext()) {
				code = iterator.next();
				if (!isAuthorised(code)) {
					return code;
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		return code;
	}

	private boolean isAuthorised(String code) throws URISyntaxException {
		try {
			URIBuilder builder = new URIBuilder(URLBase)
					.setPathSegments("services", "v2", "viralloads", "search-form")
					.addParameter("healthFacilityLabCode", code);
			restTemplate.exchange(builder.build(), HttpMethod.HEAD, new HttpEntity<>(getHeaders()),
					String.class);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
				return false;
			}
		}
		return true;
	}

	private HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		String credentials = username + ":" + password;
		byte[] base64Credentials = Base64.getEncoder().encode(credentials.getBytes());
		String basicAuthHeader = "Basic " + new String(base64Credentials);
		httpHeaders.set("Authorization", basicAuthHeader);
		return httpHeaders;
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

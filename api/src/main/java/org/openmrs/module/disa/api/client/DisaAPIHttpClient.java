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
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.TypeOfResult;
import org.openmrs.module.disa.api.config.DisaUserAgentHolder;
import org.springframework.beans.factory.annotation.Autowired;
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

	private RestTemplate restTemplate;
	private ObjectMapper objectMapper;
	private DisaUserAgentHolder userAgent;
	private String username;
	private String password;
	private String URLBase;

	@Autowired
	public DisaAPIHttpClient(
			RestTemplate restTemplate,
			ObjectMapper objectMapper,
			DisaUserAgentHolder userAgent) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.userAgent = userAgent;
	}

	public Page<LabResult> searchLabResults(
			LocalDateTime startDate,
			LocalDateTime endDate,
			String requestId,
			LabResultStatus labResultStatus,
			NotProcessingCause notProcessingCause,
			TypeOfResult typeOfResult,
			String nid,
			List<String> healthFacilityLabCodes,
			String search,
			int pageNumber,
			int pageSize,
			String orderBy,
			String direction) throws URISyntaxException {

		URIBuilder builder = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", "search")
				.addParameter("requestId", requestId)
				.addParameter("labResultStatus", labResultStatus != null ? labResultStatus.name() : "")
				.addParameter("notProcessingCause", notProcessingCause != null ? notProcessingCause.name() : "")
				.addParameter("typeOfResult", typeOfResult != null ? typeOfResult.name() : "")
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
			LabResultStatus labResultStatus,
			NotProcessingCause notProcessingCause,
			String nid,
			List<String> healthFacilityLabCodes) throws URISyntaxException {

		URIBuilder builder = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", "export")
				.addParameter("requestId", requestId)
				.addParameter("labResultStatus", labResultStatus != null ? labResultStatus.name() : null)
				.addParameter("notProcessingCause", notProcessingCause != null ? notProcessingCause.name() : null)
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

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "orgunits", "search")
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

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "orgunits", code)
				.build();

		ResponseEntity<OrgUnit> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()),
				OrgUnit.class);

		return response.getBody();

	}

	public LabResult getResultById(long id) throws URISyntaxException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
				.build();

		ResponseEntity<LabResult> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()),
				LabResult.class);

		return response.getBody();

	}

	public void deleteResultById(long id) throws IOException, URISyntaxException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(id))
				.build();

		restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(getHeaders()), String.class);

	}

	public String updateResult(LabResult labResult) throws IOException, URISyntaxException {

		URI url = new URIBuilder(URLBase)
				.setPathSegments("services", "lab-results", String.valueOf(labResult.getId()))
				.build();

		// We can't configure RestTemplate to use HttpComponentsClientHttpRequestFactory
		// to support PATCH requests due to spring-web and httpcomponents being loaded
		// by different classloaders in OpenMRS. So we'll use httpcomponents directly.
		Executor executor = Executor.newInstance()
				.authPreemptive(URLBase)
				.auth(username, password);

		Request request = Request.Patch(url)
				.addHeader("User-Agent", userAgent.get())
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
					.setPathSegments("services", "lab-results", "search")
					.addParameter("healthFacilityLabCode", code);
			restTemplate.exchange(builder.build(), HttpMethod.HEAD, new HttpEntity<>(getHeaders()),
					Void.class);
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

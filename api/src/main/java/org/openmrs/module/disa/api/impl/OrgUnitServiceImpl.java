package org.openmrs.module.disa.api.impl;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class OrgUnitServiceImpl implements OrgUnitService {

	private DisaAPIHttpClient client;

	@Autowired
	public OrgUnitServiceImpl(DisaAPIHttpClient client) {
		this.client = client;
	}

	@Override
	public OrgUnit getOrgUnitByCode(String code) {
		try {
			return client.getOrgUnitByCode(code);
		} catch (HttpStatusCodeException e) {
			throw handleHttpResponseException(e.getStatusCode().value(), "disa.orgunit.get.error");
		} catch (ResourceAccessException e) {
			if (probableConnectivityIssue(e)) {
				throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
			} else {
				throw e;
			}
		} catch (URISyntaxException e) {
			throw new DisaModuleAPIException("disa.orgunit.get.error", new Object[] { code }, e);
		}

	}

	@Override
	public List<OrgUnit> searchOrgUnits(String q) {
		try {
			return client.searchOrgUnits(q);
		} catch (HttpStatusCodeException e) {
			throw handleHttpResponseException(e.getStatusCode().value(), "disa.orgunit.search.error");
		} catch (ResourceAccessException e) {
			if (probableConnectivityIssue(e)) {
				throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
			} else {
				throw e;
			}
		} catch (URISyntaxException e) {
			throw new DisaModuleAPIException("disa.orgunit.search.error", (Object[]) null, e);
		}

	}

	private boolean probableConnectivityIssue(ResourceAccessException e) {
		return e.getCause() instanceof ConnectException
				|| e.getCause() instanceof SocketTimeoutException
				|| e.getCause() instanceof UnknownHostException;
	}

	private DisaModuleAPIException handleHttpResponseException(int statusCode, String defaultMessage) {

		HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

		if (httpStatus == HttpStatus.UNAUTHORIZED) {
			return new DisaModuleAPIException("disa.api.authentication.error", new String[] {});
		}

		if (httpStatus == HttpStatus.NOT_FOUND) {
			return new DisaModuleAPIException("disa.orgunit.not.found", (Object[]) null);
		}

		return new DisaModuleAPIException(defaultMessage, (Object[]) null);
	}
}

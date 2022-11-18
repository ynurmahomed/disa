package org.openmrs.module.disa.extension.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class RestUtil {

	private String username;
	private String password;
	private String URLBase;

	/**
	 * HTTP POST
	 *
	 * @param URLPath
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Boolean getRequestPost(String URLPath, StringEntity input) throws Exception {
		String URL = URLBase + URLPath;
		Boolean response = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			HttpPost httpPost = new HttpPost(URL);
			System.out.println(URL);
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpPost);
			httpPost.setHeader(authorizationHeader);
			httpPost.setEntity(input);
			// System.out.println("Executing request: " + httpGet.getRequestLine());
			// System.out.println(response);
			// response = httpclient.execute(httpGet,responseHandler);
			HttpResponse responseRequest = httpclient.execute(httpPost);

			if (responseRequest.getStatusLine().getStatusCode() != 204
					&& responseRequest.getStatusLine().getStatusCode() != 201) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + responseRequest.getStatusLine().getStatusCode());
			}

			httpclient.getConnectionManager().shutdown();
			response = true;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP PUT
	 *
	 * @param urlPathProcessed
	 * @param sismaCodes
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "unused" })
	public Boolean getRequestPutProcessed(String urlPathProcessed, String processedNid) throws Exception {
		String URL = URLBase + urlPathProcessed;
		Boolean response = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			uriBuilder.addParameter("processedNids", processedNid);

			HttpPut httpPut = new HttpPut(uriBuilder.build());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpPut);
			httpPut.setHeader(authorizationHeader);

			HttpResponse responseRequest = httpclient.execute(httpPut);

			/*
			 * if (responseRequest.getStatusLine().getStatusCode() != 204 &&
			 * responseRequest.getStatusLine().getStatusCode() != 201) { throw new
			 * RuntimeException("Failed : HTTP error code : " +
			 * responseRequest.getStatusLine().getStatusCode()); }
			 */

			httpclient.getConnectionManager().shutdown();
			response = true;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP PUT
	 *
	 * @param urlPathNotProcessed
	 * @param notProcessed
	 * @param reasonForNotProcessing
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "unused" })
	public Boolean getRequestPutNotProcessed(String urlPathNotProcessed, String processedNid,
			String reasonForNotProcessing) throws Exception {
		String URL = URLBase + urlPathNotProcessed;
		Boolean response = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			uriBuilder.addParameter("notProcessedNids", processedNid);
			uriBuilder.addParameter("reasonForNotProcessing", reasonForNotProcessing);

			HttpPut httpPut = new HttpPut(uriBuilder.build());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpPut);
			httpPut.setHeader(authorizationHeader);

			HttpResponse responseRequest = httpclient.execute(httpPut);

			/*
			 * if (responseRequest.getStatusLine().getStatusCode() != 204 &&
			 * responseRequest.getStatusLine().getStatusCode() != 201) { throw new
			 * RuntimeException("Failed : HTTP error code : " +
			 * responseRequest.getStatusLine().getStatusCode()); }
			 */

			httpclient.getConnectionManager().shutdown();
			response = true;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP PUT
	 *
	 * @param urlPathPending
	 * @param nids
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Boolean getRequestPutPending(String urlPathPending, List<String> nids) throws Exception {
		String URL = URLBase + urlPathPending;
		Boolean response = false;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			for (String nid : nids) {
				uriBuilder.addParameter("pendingNids", nid);
			}

			HttpPut httpPut = new HttpPut(uriBuilder.build());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpPut);
			httpPut.setHeader(authorizationHeader);

			httpclient.execute(httpPut);

			httpclient.getConnectionManager().shutdown();
			response = true;
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP GET
	 *
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String getRequestGet(List<String> sismaCodes, String requestingProvinceName) throws Exception {
		String URL = URLBase;
		String response = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("locationCodes", sismaCode);
				uriBuilder.addParameter("requestingProvinceName", requestingProvinceName);
			}

			HttpGet httpGet = new HttpGet(uriBuilder.build());

			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpGet);
			httpGet.setHeader(authorizationHeader);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			/*
			 * HttpParams params = new BasicHttpParams();
			 * params.setParameter("sismaCodes", sismaCodes);
			 * params.setParameter("requestingProvinceName", requestingProvinceName);
			 * httpclient.setParams(params);
			 */

			response = httpclient.execute(httpGet, responseHandler);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP GET
	 *
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String getRequestGet(List<String> sismaCodes) throws Exception {
		String URL = URLBase;
		String response = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("locationCodes", sismaCode);
			}

			HttpGet httpGet = new HttpGet(uriBuilder.build());

			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpGet);
			httpGet.setHeader(authorizationHeader);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			/*
			 * HttpParams params = new BasicHttpParams();
			 * params.setParameter("sismaCodes", sismaCodes);
			 * params.setParameter("requestingProvinceName", requestingProvinceName);
			 * httpclient.setParams(params);
			 */

			response = httpclient.execute(httpGet, responseHandler);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP GET
	 *
	 * @param sismaCodes
	 * @param sismaCodes
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String getRequestGetFsrByStatus(String urlPathProcessed, List<String> sismaCodes, String viralLoadStatus)
			throws Exception {
		String URL = URLBase + urlPathProcessed;
		String response = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();

		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			uriBuilder.addParameter("locationCodes", sismaCodes.get(0));
			uriBuilder.addParameter("viralLoadStatus", viralLoadStatus);

			HttpGet httpGet = new HttpGet(uriBuilder.build());

			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpGet);
			httpGet.setHeader(authorizationHeader);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			response = httpclient.execute(httpGet, responseHandler);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 * HTTP GET
	 *
	 * @param sismaCodes
	 * @param sismaCodes
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String getRequestGetFsrByStatusAndDates(String urlPathProcessed, List<String> sismaCodes,
			String viralLoadStatus, String startDate, String endDate) throws Exception {
		String URL = URLBase + urlPathProcessed;
		String response = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();

		try {

			URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("locationCodes", sismaCode);
			}
			uriBuilder.addParameter("viralLoadStatus", viralLoadStatus);
			uriBuilder.addParameter("startDate", startDate);
			uriBuilder.addParameter("endDate", endDate);

			HttpGet httpGet = new HttpGet(uriBuilder.build());

			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpGet);
			httpGet.setHeader(authorizationHeader);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			response = httpclient.execute(httpGet, responseHandler);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	/**
	 *
	 * @param urlPathProcessed
	 * @param requestId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String getRequestByForm(String urlPathProcessed, String requestId, String nid,
			String vlSisma, String referringId, String vlState, String startDate, String endDate) throws Exception {
		String URL = URLBase + urlPathProcessed;
		String response = "";
		DefaultHttpClient httpclient = new DefaultHttpClient();

		try {

			URIBuilder uriBuilder = new URIBuilder(URL);

			if (!requestId.isEmpty()) {
				uriBuilder.addParameter("requestId", requestId);
			}

			if (!nid.isEmpty()) {
				uriBuilder.addParameter("nid", nid);
			}

			if (!vlSisma.isEmpty() && !vlSisma.equals(Constants.TODOS)) {
				uriBuilder.addParameter("healthFacilityLabCode", vlSisma);
			}

			if (!referringId.isEmpty()) {
				uriBuilder.addParameter("referringRequestID", referringId);
			}

			if (!vlState.isEmpty() && !vlState.equals(Constants.ALL)) {
				uriBuilder.addParameter("viralLoadStatus", vlState);
			}

			if (!startDate.isEmpty()) {
				uriBuilder.addParameter("startDate", startDate);
			}

			if (!endDate.isEmpty()) {
				uriBuilder.addParameter("endDate", endDate);
			}

			HttpGet httpGet = new HttpGet(uriBuilder.build());

			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			BasicScheme scheme = new BasicScheme();
			Header authorizationHeader = scheme.authenticate(credentials, httpGet);
			httpGet.setHeader(authorizationHeader);
			ResponseHandler<String> responseHandler = new BasicResponseHandler();

			response = httpclient.execute(httpGet, responseHandler);

		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return response;
	}

	public void delete(String requestId) throws IOException, URISyntaxException {
		URI url;

		url = new URIBuilder(URLBase)
				.setPath(String.format("/services/viralloads/%s", requestId))
				.build();

		Executor executor = Executor.newInstance()
				.auth(username, password);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		executor.execute(Request.Delete(url))
				.handleResponse(responseHandler);
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

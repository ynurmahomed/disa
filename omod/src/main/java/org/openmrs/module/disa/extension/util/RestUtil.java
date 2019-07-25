package org.openmrs.module.disa.extension.util;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class RestUtil {

	private String username;
	private String password;
	private String URLBase;
	
	/**
	 * HTTP POST
	 * @param URLPath
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public Boolean getRequestPost(String URLPath, StringEntity input) throws Exception {
        String URL = URLBase + URLPath;
        Boolean response =  false;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
        	HttpPost httpPost = new HttpPost(URL);
        	System.out.println(URL);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpPost);
            httpPost.setHeader(authorizationHeader);
            httpPost.setEntity(input);
            //System.out.println("Executing request: " + httpGet.getRequestLine());
            //System.out.println(response);
//            response = httpclient.execute(httpGet,responseHandler);
            HttpResponse responseRequest = httpclient.execute(httpPost);
            
    		if (responseRequest.getStatusLine().getStatusCode() != 204 && responseRequest.getStatusLine().getStatusCode() != 201) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ responseRequest.getStatusLine().getStatusCode());
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
	 * @param urlPathProcessed 
	 * @param sismaCodes
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public Boolean getRequestPutProcessed(String urlPathProcessed, List<String> sismaCodes) throws Exception {
        String URL = URLBase + urlPathProcessed;
        Boolean response =  false;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
        	
        	URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("processedNids",sismaCode);
			}
        	
        	HttpPut httpPut = new HttpPut(uriBuilder.build()); 
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpPut);
            httpPut.setHeader(authorizationHeader);

            HttpResponse responseRequest = httpclient.execute(httpPut);
            
    		/*if (responseRequest.getStatusLine().getStatusCode() != 204 && responseRequest.getStatusLine().getStatusCode() != 201) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ responseRequest.getStatusLine().getStatusCode());
    		}*/
    		
    		httpclient.getConnectionManager().shutdown();
    		response = true;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }
	
	/**
	 * HTTP PUT
	 * @param urlPathNotProcessed  
	 * @param sismaCodes
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public Boolean getRequestPutNotProcessed(String urlPathNotProcessed , List<String> sismaCodes) throws Exception {
        String URL = URLBase + urlPathNotProcessed;
        Boolean response =  false;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
        	
        	URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("notProcessedNids",sismaCode);
			}
        	
        	HttpPut httpPut = new HttpPut(uriBuilder.build()); 
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpPut);
            httpPut.setHeader(authorizationHeader);

            HttpResponse responseRequest = httpclient.execute(httpPut);
            
    		/*if (responseRequest.getStatusLine().getStatusCode() != 204 && responseRequest.getStatusLine().getStatusCode() != 201) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ responseRequest.getStatusLine().getStatusCode());
    		}*/
    		
    		httpclient.getConnectionManager().shutdown();
    		response = true;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
    }
	
	/**
	 * HTTP GET
	 * @param URLPath
	 * @return
	 * @throws Exception
	 */
	public String getRequestGet(List<String> sismaCodes) throws Exception {
        String URL = URLBase;
        String response =  "";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
        	
        	URIBuilder uriBuilder = new URIBuilder(URL);
			for (String sismaCode : sismaCodes) {
				uriBuilder.addParameter("locationCodes",sismaCode);
			}
			
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            BasicScheme scheme = new BasicScheme();
            Header authorizationHeader = scheme.authenticate(credentials, httpGet);
            httpGet.setHeader(authorizationHeader);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            HttpParams params = new BasicHttpParams();
            params.setParameter("sismaCodes", sismaCodes);
            httpclient.setParams(params);
            response = httpclient.execute(httpGet,responseHandler);
            
           
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return response;
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

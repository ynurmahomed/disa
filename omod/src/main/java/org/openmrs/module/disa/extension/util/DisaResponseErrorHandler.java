package org.openmrs.module.disa.extension.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class DisaResponseErrorHandler implements ResponseErrorHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ResponseErrorHandler.class);

	@Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus.Series series = response.getStatusCode().series();
        return (series == HttpStatus.Series.CLIENT_ERROR || series == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String errorMessage = IOUtils.toString(response.getBody(), StandardCharsets.UTF_8);
        logger.error("RestTemplate request failed with status code: {}, message: {}", response.getStatusCode(), errorMessage);
    }

}

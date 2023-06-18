package org.openmrs.module.disa.extension.util;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NotificationUtil {

	public static ResponseEntity<String> sendEmail(String endpointUrl, String to, 
												   String subject, String body, 
												   String module){

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DisaResponseErrorHandler()); 
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("data", getDataIntoJsonNode(to, subject, body, module));
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
		
		try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(endpointUrl, requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok("Email sent successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email. Response code: " + responseEntity.getStatusCode());
            }
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email. Error message: " + e.getMessage());
        }
	}

	public static Object getDataIntoJsonNode(String to, String subject, String body, String module) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode dataNode = objectMapper.createObjectNode();
		ArrayNode arrayNode = objectMapper.createArrayNode();
		dataNode.set("to", GenericUtil.populateArrayNode(to.split(","),arrayNode)); 
        dataNode.put("subject", subject);
        dataNode.put("body", body);
        dataNode.put("module", module);
        dataNode.put("startDate", StringUtils.EMPTY);
        dataNode.put("endDate", StringUtils.EMPTY);

		return dataNode;
	}
}

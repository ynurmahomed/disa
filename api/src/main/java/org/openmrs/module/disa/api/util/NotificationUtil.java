package org.openmrs.module.disa.api.util;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class NotificationUtil {

    private static final Logger log = LoggerFactory.getLogger(NotificationUtil.class);

    public void sendEmail(String endpointUrl, String to,
            String subject, String body,
            String module, String startDate, String endDate, String repoLink ,String resultFlag) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DisaResponseErrorHandler());
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("data", getDataIntoJsonNode(to, subject, body, module, startDate, endDate, repoLink, resultFlag));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(endpointUrl, requestEntity,
                    String.class);
            if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully.");
            } else if (responseEntity != null) {
                log.error("Failed to send email. Response code: {}", responseEntity.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Failed to send email.", e);
        }
    }

    private Object getDataIntoJsonNode(String to, String subject, String body, String module, String startDate, String endDate, String repoLink, String resultFlag) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dataNode = objectMapper.createObjectNode();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        dataNode.set("to", GenericUtil.populateArrayNode(to.split(","), arrayNode));
        dataNode.put("subject", subject);
        dataNode.put("body", body);
        dataNode.put("module", module);
        dataNode.put("startDate", StringUtils.EMPTY);
        dataNode.put("endDate", StringUtils.EMPTY);
        dataNode.put("repoLink", StringUtils.EMPTY);
        dataNode.put("resultFlag", StringUtils.EMPTY);

        return dataNode;
    }
}

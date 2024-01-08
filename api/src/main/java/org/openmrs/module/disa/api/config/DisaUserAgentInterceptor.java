package org.openmrs.module.disa.api.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class DisaUserAgentInterceptor implements ClientHttpRequestInterceptor {

    private final DisaUserAgentHolder userAgent;

    public DisaUserAgentInterceptor(DisaUserAgentHolder userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        request.getHeaders().add("User-Agent", userAgent.get());
        return execution.execute(request, body);
    }
}

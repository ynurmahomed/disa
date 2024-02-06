package org.openmrs.module.disa.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.disa.api.config.DisaUserAgentHolder;
import org.openmrs.module.disa.api.config.DisaUserAgentInterceptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

public class DisaUserAgentInterceptorTest {

    private String userAgent = "disa/X.Y.Z";

    private DisaUserAgentInterceptor disaUserAgentInterceptor;

    @Before
    public void setUp() {
        disaUserAgentInterceptor = new DisaUserAgentInterceptor(new DisaUserAgentHolder(userAgent));
    }

    @Test
    public void interceptShouldAddUserAgentHeader() throws IOException {

        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpHeaders httpHeaders = mock(HttpHeaders.class);

        when(httpRequest.getHeaders()).thenReturn(httpHeaders);

        disaUserAgentInterceptor.intercept(httpRequest, null, mock(ClientHttpRequestExecution.class));

        verify(httpHeaders).add("User-Agent", userAgent);
    }

}

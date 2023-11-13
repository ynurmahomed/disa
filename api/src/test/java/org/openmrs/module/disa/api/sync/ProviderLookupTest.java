package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.test.BaseContextMockTest;

public class ProviderLookupTest extends BaseContextMockTest {

    @Mock
    private UserService userService;

    @Mock
    private ProviderService providerService;

    @Mock
    private LabResultHandler next;

    @InjectMocks
    private ProviderLookup providerHandler;

    @Before
    public void before() {
        providerHandler.setNext(next);
    }

    @Test
    public void shouldAddProviderToSyncContext() {

        LabResult labResult = new HIVVLLabResult();
        labResult.setLabResultStatus(LabResultStatus.PENDING);
        User user = new User();
        user.setPerson(new Person());
        Provider provider = new Provider();

        when(userService.getUserByUsername(anyString()))
                .thenReturn(user);

        when(providerService.getProvidersByPerson(anyObject()))
                .thenReturn(Arrays.asList(provider));

        providerHandler.handle(labResult);

        assertThat(providerHandler.getSyncContext().get(ProviderLookup.PROVIDER_KEY), is(provider));

        // Calls the next handler
        verify(next, times(1)).handle(labResult);
    }
}

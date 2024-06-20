package org.openmrs.module.disa.api.util;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.test.BaseContextMockTest;

public class NotifierTest extends BaseContextMockTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private LocationService locationService;

    @Mock
    private NotificationUtil notificationUtil;

    @InjectMocks
    private Notifier notifier;

    @Test
    public void notifyShouldNotSendEmailWithoutRecipients() {
        GlobalProperty mailToGp = new GlobalProperty("disa.api.mail.to", "");
        when(administrationService.getGlobalPropertyObject(anyString())).thenReturn(mailToGp);
        verify(notificationUtil, never()).sendEmail(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }
}

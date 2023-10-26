package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.sync.scheduler.ViralLoadFormSchedulerTask;
import org.openmrs.test.BaseContextMockTest;

public class SyncStatusServiceTest extends BaseContextMockTest {

    private static final long REPEAT_INTERVAL = 3600l; // 1h

    private static final String HOUR = "h";

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private DisaService disaService;

    @InjectMocks
    private SyncStatusService syncStatusService;

    @Before
    public void setUp() {
        when(userContext.getLocale()).thenReturn(Locale.getDefault());
        when(messageSourceService.getMessage("Scheduler.scheduleForm.repeatInterval.units.hours")).thenReturn(HOUR);
        when(disaService.getSyncTaskRepeatInterval()).thenReturn(REPEAT_INTERVAL);
    }

    @Test
    public void getLastExecutionMessageShouldContainLastExecutionAndRepeatInterval() {
        LocalDateTime lastExecutionTime = LocalDateTime.now();
        SyncStatus syncStatus = new SyncStatus(lastExecutionTime, 0l, false, lastExecutionTime, 0f);

        ViralLoadFormSchedulerTask.setSyncStatus(syncStatus);

        syncStatusService.getLastExecutionMessage();

        ArgumentCaptor<String[]> argCaptor = ArgumentCaptor.forClass(String[].class);
        verify(messageSourceService, times(1)).getMessage(
                anyString(),
                argCaptor.capture(),
                any(Locale.class));

        assertThat(argCaptor.getValue(),
                arrayContaining(
                        allOf(containsString("" + lastExecutionTime.getDayOfMonth()),
                                containsStringIgnoringCase(lastExecutionTime.getMonth().name()),
                                containsString("" + lastExecutionTime.getYear())),
                        allOf(containsString("" + lastExecutionTime.getHour()),
                                containsString("" + lastExecutionTime.getMinute())),
                        allOf(containsString("1"),
                                containsString(HOUR))));
    }

    @Test
    public void getLastExecutionMessageShouldReturnNull() {
        SyncStatus syncStatus = new SyncStatus(null, 0l, false, null, 0f);

        ViralLoadFormSchedulerTask.setSyncStatus(syncStatus);

        String result = syncStatusService.getLastExecutionMessage();

        assertThat(result, nullValue());
    }

    @Test
    public void getCurrentExecutionMessageShouldReturnStartedExecutionAndProgress() {
        LocalDateTime startedExecutionTime = LocalDateTime.now();
        boolean executing = true;
        float progress = 0.5f;

        SyncStatus syncStatus = new SyncStatus(null, 0, executing, startedExecutionTime, progress);

        ViralLoadFormSchedulerTask.setSyncStatus(syncStatus);
        syncStatusService.getCurrentExecutionMessage();

        ArgumentCaptor<String[]> argCaptor = ArgumentCaptor.forClass(String[].class);
        verify(messageSourceService, times(1)).getMessage(
                anyString(),
                argCaptor.capture(),
                any(Locale.class));

        assertThat(argCaptor.getValue(),
                arrayContaining(
                        allOf(containsString("50"),
                                containsString("%")),
                        allOf(containsString("" + startedExecutionTime.getDayOfMonth()),
                                containsStringIgnoringCase(startedExecutionTime.getMonth().name()),
                                containsString("" + startedExecutionTime.getYear())),
                        allOf(containsString("" + startedExecutionTime.getHour()),
                                containsString("" + startedExecutionTime.getMinute()))));
    }

    @Test
    public void testGetCurrentExecutionMessageNotExecuting() {
        boolean executing = false;
        SyncStatus syncStatus = new SyncStatus(null, 0, executing, null, 0f);

        ViralLoadFormSchedulerTask.setSyncStatus(syncStatus);

        String result = syncStatusService.getCurrentExecutionMessage();

        assertThat(result, nullValue());
    }
}

package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.test.BaseContextMockTest;

public class LabResultProcessorTest extends BaseContextMockTest {

    @Mock
    LabResultHandler labResultHandler;

    @InjectMocks
    private LabResultProcessor labResultProcessor;

    @Test
    public void shouldCountResultsByStatus() {
        when(labResultHandler.handle(any(LabResult.class)))
            .thenReturn(LabResultStatus.PROCESSED, LabResultStatus.NOT_PROCESSED);

        labResultProcessor.processResult(mock(LabResult.class));
        labResultProcessor.processResult(mock(LabResult.class));

        assertThat(labResultProcessor.getProcessedCount(), is(1));
        assertThat(labResultProcessor.getNotProcessedCount(), is(1));
    }
}

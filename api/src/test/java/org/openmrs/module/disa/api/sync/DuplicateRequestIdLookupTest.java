package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.test.BaseContextMockTest;

public class DuplicateRequestIdLookupTest extends BaseContextMockTest {

    @Mock
    private DisaService disaService;

    @InjectMocks
    private DuplicateRequestIdLookup duplicateRequestIdHandler;

    @Test
    public void shouldSetLabResultAsNotProcessedDuplicateRequestId() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        when(disaService.existsInSyncLog(labResult))
                .thenReturn(true);

        duplicateRequestIdHandler.handle(labResult);

        assertThat(labResult.getLabResultStatus(), is(LabResultStatus.NOT_PROCESSED));
        assertThat(labResult.getNotProcessingCause(), is(NotProcessingCause.DUPLICATED_REQUEST_ID));
    }

    @Test
    public void shouldCallNextHandler() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setLabResultStatus(LabResultStatus.PENDING);

        when(disaService.existsInSyncLog(labResult))
                .thenReturn(true);

        LabResultHandler next = Mockito.mock(LabResultHandler.class);
        duplicateRequestIdHandler.setNext(next);

        duplicateRequestIdHandler.handle(labResult);

        verify(next, times(1)).handle(labResult);
    }

}

package org.openmrs.module.disa.api.sync.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.sync.LabResultProcessor;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseContextMockTest;

public class ViralLoadFormSchedulerTaskTest extends BaseContextMockTest {

	private static final Long REPEAT_INTERVAL = 3600l; // 1h

	@Mock
	private LabResultService labResultService;
	@Mock
	private LabResultProcessor labResultProcessor;

	// Used in Context.openSession()
	@Mock
	private ContextDAO contextDAO;

	@Mock
	private TaskDefinition taskDefinition;

	@InjectMocks
	private ViralLoadFormSchedulerTask task;

	private LabResult labResult;

	@Before
	public void setUp() throws Exception {
		labResult = new HIVVLLabResult();
		labResult.setRequestId("MZDISAPQM0000000");
		labResult.setNid("000000000/0000/00000");
		labResult.setHealthFacilityLabCode("1040107");
		labResult.setPregnant("");
		labResult.setBreastFeeding("");
		labResult.setReasonForTest("");
		labResult.setPrimeiraLinha("");
		labResult.setSegundaLinha("");

		when(taskDefinition.getRepeatInterval()).thenReturn(REPEAT_INTERVAL);

		ViralLoadFormSchedulerTask.setEnvironment("PROD");

		task.initialize(taskDefinition);
	}

	@Test
	public void shouldLoadLabResultsToSynchronize() {
		when(labResultService.getResultsToSync()).thenReturn(Arrays.asList(labResult));

		task.execute();

		verify(labResultService).getResultsToSync();
	}

	@Test
	public void shouldProcessLabResults() {
		when(labResultService.getResultsToSync()).thenReturn(Arrays.asList(labResult));

		task.execute();

		verify(labResultProcessor).processResult(any(HIVVLLabResult.class));
	}

	@Test
	public void executeShouldNotSyncWhenEnvironmentIsNotProd() {

		ViralLoadFormSchedulerTask.setEnvironment(null);

		task.execute();

		verify(labResultProcessor, never()).processResult(any(HIVVLLabResult.class));
	}

}

package org.openmrs.module.disa.api.sync.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.sync.LabResultProcessor;
import org.openmrs.module.disa.api.sync.SyncStatus;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.api.util.GenericUtil;
import org.openmrs.module.disa.api.util.Notifier;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author machabane
 *
 */
public class ViralLoadFormSchedulerTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(ViralLoadFormSchedulerTask.class);

	private static SyncStatus syncStatus = SyncStatus.initial();

	private static String environment;

	private LabResultService labResultService;

	private LabResultProcessor labResultProcessor;

	private Notifier notifier;

	public static SyncStatus getSyncStatus() {
		return syncStatus;
	}

	public static void setSyncStatus(SyncStatus status) {
		syncStatus = status;
	}

	public static void setEnvironment(String env) {
		environment = env;
	}

	public ViralLoadFormSchedulerTask() {
		this.labResultProcessor = Context.getRegisteredComponents(LabResultProcessor.class).get(0);
		this.labResultService = Context.getRegisteredComponents(LabResultService.class).get(0);
		this.notifier = Context.getRegisteredComponents(Notifier.class).get(0);
	}

	public ViralLoadFormSchedulerTask(LabResultProcessor labResultProcessor, LabResultService labResultService,
			Notifier notifier) {
		this.labResultProcessor = labResultProcessor;
		this.labResultService = labResultService;
		this.notifier = notifier;
	}

	@Override
	public void execute() {
		logger.info("module started...");
		Context.openSession();
		try {
			syncStatus = syncStatus.started();
			createViralLoadForm();
		} catch (Exception e) {
			logger.error("O erro ", e);
			notifier.notify(
					Constants.DISA_NOTIFICATION_ERROR_SUBJECT,
					GenericUtil.getStackTrace(e),
					Constants.DISA_MODULE,
					"",
					"",
					"",
					null); 
		} finally {
			syncStatus = syncStatus.ended();
		}
		Context.closeSession();
		logger.info("module ended.");
	}

	private void createViralLoadForm() {

		if (environment == null || !environment.equalsIgnoreCase("PROD")) {
			logger.info("Will not sync lab results because environment is not PROD.");
			return;
		}

		List<LabResult> labResults = labResultService.getResultsToSync();
		logger.info("There is {} pending items to be processed", labResults.size());

		logger.info("Syncing started...");

		// iterate the viral load list and create the encounters
		for (int i = 0; i < labResults.size(); i++) {
			LabResult labResult = labResults.get(i);
			logger.debug("Processing {}", labResult);
			labResultProcessor.processResult(labResult);
			syncStatus = syncStatus.withProgress(i / (float) labResults.size());
		}

		logger.debug("Sync summary: PENDING={}, PROCESSED={}, NOT_PROCESSED={} ",
				labResults.size(), labResultProcessor.getProcessedCount(),
				labResultProcessor.getNotProcessedCount());

		logger.info("Syncing ended ({}s).",
				Duration.between(syncStatus.getStartedExecutionTime(), LocalDateTime.now()).getSeconds());
	}
}

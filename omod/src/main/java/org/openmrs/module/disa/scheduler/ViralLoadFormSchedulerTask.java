package org.openmrs.module.disa.scheduler;

import java.util.List;

import org.apache.http.conn.HttpHostConnectException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.sync.LabResultProcessor;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.extension.util.GenericUtil;
import org.openmrs.module.disa.extension.util.NotificationUtil;
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

	private LabResultService labResultService;

	private LabResultProcessor labResultProcessor;
	
	private String disaNotificationUrl;
	
	private String disaNotificationMailTo;
	
	private AdministrationService administrationService;
	
	private LocationService locationService;
	
	public ViralLoadFormSchedulerTask() {
		this.labResultProcessor = Context.getRegisteredComponents(LabResultProcessor.class).get(0);
		this.labResultService = Context.getRegisteredComponents(LabResultService.class).get(0);
		this.administrationService = Context.getAdministrationService();
		this.locationService = Context.getLocationService();
		disaNotificationUrl = administrationService.
				getGlobalPropertyObject(Constants.DISA_API_NOTIFICATION_URL).getPropertyValue();
		disaNotificationMailTo = administrationService.
				getGlobalPropertyObject(Constants.DISA_API_MAIL_TO).getPropertyValue();
	}

	public ViralLoadFormSchedulerTask(LabResultProcessor labResultProcessor, LabResultService labResultService) {
		this.labResultProcessor = labResultProcessor;
		this.labResultService = labResultService;
	}

	@Override
	public void execute() {
		logger.info("module started...");
		Context.openSession();
		try {
			createViralLoadForm();
		} catch (HttpHostConnectException e) {
			// ignora a exception
		} catch (Exception e) {
			logger.error("O erro ", e);
			NotificationUtil.sendEmail(
			  disaNotificationUrl,   
			  disaNotificationMailTo,   
			  Constants.DISA_NOTIFICATION_ERROR_SUBJECT+locationService.getDefaultLocation().getName(),
			  GenericUtil.getStackTrace(e),   
			  Constants.DISA_MODULE); 
		}
		Context.closeSession();
		logger.info("module ended...");
	}

	private void createViralLoadForm() throws HttpHostConnectException {

		List<LabResult> labResults = labResultService.getResultsToSync();

		logger.info("There is {} pending items to be processed", labResults.size());

		logger.info("Syncing started...");

		// iterate the viral load list and create the encounters
		for (LabResult labResult : labResults) {
			logger.debug("Processing {}", labResult);
			labResultProcessor.processResult(labResult);
		}

		logger.debug("Sync summary: PENDING={}, PROCESSED={}, NOT_PROCESSED={} ",
				labResults.size(), labResultProcessor.getProcessedCount(), labResultProcessor.getNotProcessedCount());

		logger.info("Syncing ended...");
	}
}

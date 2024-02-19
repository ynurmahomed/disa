/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.disa;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.sync.scheduler.ViralLoadFormSchedulerTask;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.scheduler.TaskDefinition;

/**
 * This class contains the logic that is run every time this module is either
 * started or stopped.
 */
public class DisaModuleActivator extends BaseModuleActivator {

	protected Log log = LogFactory.getLog(getClass());

	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Disa Module Module");
	}

	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Disa Module Module refreshed");

		setUpDisaHttpClient();
	}

	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Disa Module Module");
		Location defaultLocation = Context.getLocationService().getDefaultLocation();
		if (defaultLocation == null
				|| "Unknown Location".equals(defaultLocation.getName())) {
			throw new ModuleException(
					"Não foi possivel carregar a default location. Certifique que foi devidamente configurada.");
		}
	}

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		log.info("Disa Module Module started");

		setUpDisaHttpClient();
		setUpViralLoadFormSchedulerTask();
	}

	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Disa Module Module");
	}

	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Disa Module Module stopped");
	}

	private void setUpDisaHttpClient() {
		DisaAPIHttpClient disaAPIHttpClient = Context.getRegisteredComponents(DisaAPIHttpClient.class).get(0);

		AdministrationService administrationService = Context.getAdministrationService();
		disaAPIHttpClient.setURLBase(administrationService.getGlobalPropertyValue(Constants.DISA_URL, ""));

		try {
			ViralLoadFormSchedulerTask.setEnvironment(System.getenv("DISA_ENVIRONMENT"));
		} catch (NullPointerException e) {
			// No problem if environment is not configured. Task won't sync lab results.
		}

		try {
			disaAPIHttpClient.setUsername(System.getenv("DISA_API_USERNAME"));
		} catch (NullPointerException e) {
			disaAPIHttpClient.setUsername("");
		}

		try {
			disaAPIHttpClient.setPassword(System.getenv("DISA_API_PASSWORD"));
		} catch (NullPointerException e) {
			disaAPIHttpClient.setPassword("");
		}
	}

	private void setUpViralLoadFormSchedulerTask() {
		Collection<TaskDefinition> scheduledTasks = Context.getSchedulerService().getRegisteredTasks();
		Optional<TaskDefinition> taskDefinition = scheduledTasks.stream()
				.filter(s -> s.getTaskClass().equals(ViralLoadFormSchedulerTask.class.getName()))
				.findFirst();

		if (!taskDefinition.isPresent()) {
			TaskDefinition task = new TaskDefinition();
			task.setName("ViralLoadSchedulerTask");
			task.setTaskClass(ViralLoadFormSchedulerTask.class.getName());
			task.setStartOnStartup(true);
			task.setRepeatInterval(30 * 60l);
			task.setStarted(true);
			Context.getSchedulerService().saveTaskDefinition(task);
		}
	}
}

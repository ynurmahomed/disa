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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.dataexchange.DataImporter;

/**
 * This class contains the logic that is run every time this module is either
 * started or stopped.
 */
public class DisaModuleActivator extends BaseModuleActivator {

	protected Log log = LogFactory.getLog(this.getClass());

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
	}

	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Disa Module Module");
	}

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		this.installMetaData();
		log.info("Disa Module Module started");
	}

	private void installMetaData() {
		DataImporter dataImporter = Context.getRegisteredComponents(DataImporter.class).get(0);

		log.info("Importing patient_identifier_type Metadata");
		dataImporter.importData("patient-identifier-types.xml");
		log.info("patient_identifier_type Metadata imported");

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

}

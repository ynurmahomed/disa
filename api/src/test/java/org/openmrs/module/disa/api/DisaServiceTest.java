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
package org.openmrs.module.disa.api;

import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Tests {@link ${DisaService}}.
 */
public class DisaServiceTest extends BaseModuleContextSensitiveTest {

	@Autowired
	EncounterService encounterService;

	@Autowired
	PatientService patientService;

	@Autowired
	LocationService locationService;

	@Autowired
	FormService formService;

	@Autowired
	ProviderService providerService;

	@Autowired
	PersonService personService;

	@Autowired
	ConceptService conceptService;

	@Autowired
	ObsService obsService;

	@Test
	public void shouldSetupContext() {
		executeDataSet("org/openmrs/module/disa/api/include/DisaServiceTest-shouldSetupContext.xml");
		assertNotNull(Context.getService(DisaService.class));
	}

	@Test
	public void fsrEncounterShouldNotBeNull() throws ParseException {

		executeDataSet("org/openmrs/module/disa/api/include/DisaServiceTest-fsrEncounterShouldNotBeNull.xml");

		Set<Obs> obsSet = new HashSet<Obs>();

		Encounter encounter = new Encounter();

		encounter.setEncounterDatetime(new Date());
		encounter.setPatient(patientService.getPatientByUuid("fe92bf4b-515c-443b-8791-8f88d2ee4ec8"));
		encounter.setLocation(locationService.getLocationByUuid("27013587-1ca0-4e38-ab78-0c6a1d59ceb5"));
		encounter.setForm(formService.getFormByUuid("5b7cecc3-4ba3-4710-85ae-fc0c13e83e27"));
		encounter.setProvider(encounterService.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66"),
				providerService.getProviderByUuid("e2bc3932-1d5f-11e0-b929-000c29ad1d07"));
		encounter.setEncounterType(encounterService.getEncounterTypeByUuid("b5b7d21f-efd1-407e-81ce-ba9d93c524f8"));

		Obs obs = new Obs();
		obs.setPerson(personService.getPersonByUuid("ae695be7-a586-42a7-b90f-4d0960929f28"));
		obs.setObsDatetime(new Date());
		obs.setConcept(conceptService.getConceptByUuid("e173835b-135c-4fab-9b5e-b255565980e5"));
		obs.setValueText("PNC");

		obsSet.add(obs);

		encounter.setObs(obsSet);

		Encounter savedEncounter = encounterService.saveEncounter(encounter);

		Assert.notNull(savedEncounter.getUuid());
	}
}

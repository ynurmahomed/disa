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
package org.openmrs.module.disa.api.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.SyncLog;
import org.openmrs.module.disa.api.db.DisaDAO;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * It is a default implementation of {@link DisaService}.
 */
@Service
public class DisaServiceImpl extends BaseOpenmrsService implements DisaService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private EncounterService encounterService;
	private LabResultService labResultService;
	private PatientService patientService;
	private LocationService locationService;
	private MessageSourceService messageSourceService;
	private DisaDAO dao;

	@Autowired
	public DisaServiceImpl(
			DisaDAO dao,
			LabResultService labResultService,
			@Qualifier("patientService") PatientService patientService,
			@Qualifier("locationService") LocationService locationService,
			EncounterService encounterService,
			MessageSourceService messageSourceService) {
		this.dao = dao;
		this.labResultService = labResultService;
		this.patientService = patientService;
		this.locationService = locationService;
		this.encounterService = encounterService;
		this.messageSourceService = messageSourceService;
	}

	@Override
	public List<LocationAttribute> getAllLocationAttribute(String valueReference) {
		return dao.getAllLocationAttribute(valueReference);
	}

	@Override
	public Serializable saveSyncLog(SyncLog fsrLog) {
		return dao.saveSyncLog(fsrLog);
	}

	@Override
	public boolean existsInSyncLog(LabResult labResult) {
		return dao.existsByRequestIdAndTypeOfResult(labResult.getRequestId(), labResult.getTypeOfResult());
	}

	@Override
	public List<Integer> getPatientByNid(String identifier) {
		return dao.getPatientByNid(identifier);
	}

	@Override
	public List<Patient> getPatientByPatientId(Integer patientId) {
		return dao.getPatientByPatientId(patientId);
	}

	@Override
	public Patient mapIdentifier(String patientUuid, LabResult disa) {

		if (disa.getLabResultStatus() != LabResultStatus.NOT_PROCESSED
				|| disa.getNotProcessingCause() != NotProcessingCause.NID_NOT_FOUND) {

			String labResultStatus = messageSourceService
					.getMessage("disa.viral.load.status." + disa.getLabResultStatus());
			throw new DisaModuleAPIException("disa.viralload.map.wrong.status",
					new String[] { labResultStatus });
		}

		Patient patient = patientService.getPatientByUuid(patientUuid);
		if (patient.getActiveIdentifiers().isEmpty()) {
			throw new DisaModuleAPIException("disa.map.no.identifiers", new Object[] {});
		}

		PatientIdentifierType disaNIDIdentifier = patientService
				.getPatientIdentifierTypeByUuid(Constants.DISA_NID);
		List<PatientIdentifier> identifiers = patientService.getPatientIdentifiers(disa.getNid(),
				Arrays.asList(disaNIDIdentifier), null, null, null);

		if (identifiers.isEmpty()) {

			PatientIdentifier patientIdentifier = new PatientIdentifier();
			patientIdentifier.setPatient(patient);
			patientIdentifier.setIdentifier(disa.getNid());
			patientIdentifier.setIdentifierType(disaNIDIdentifier);
			patientIdentifier.setLocation(locationService.getDefaultLocation());
			patientService.savePatientIdentifier(patientIdentifier);

			List<LabResult> notProcessed = getNotProcessedForPatient(disa);
			for (LabResult l : notProcessed) {
				labResultService.rescheduleLabResult(l.getId());
			}
		}

		return patient;
	}

	private List<LabResult> getNotProcessedForPatient(LabResult disa) {
		return labResultService.getAll(null, null, null, LabResultStatus.NOT_PROCESSED, null, disa.getNid(),
				Collections.singletonList(disa.getHealthFacilityLabCode()));
	}

	public void handleProcessedLabResult(LabResult labResult, Encounter encounter) {
		encounterService.saveEncounter(encounter);

		SyncLog fsrLog = new SyncLog();
		fsrLog.setPatientId(encounter.getPatient().getPatientId());
		fsrLog.setEncounter(encounter);
		fsrLog.setPatientIdentifier(labResult.getNid());
		fsrLog.setRequestId(labResult.getRequestId());
		fsrLog.setCreator(Context.getAuthenticatedUser().getId());
		fsrLog.setDateCreated(new Date());
		fsrLog.setTypOfResult(labResult.getTypeOfResult());
		saveSyncLog(fsrLog);

		String defaultLocationUuid = locationService.getDefaultLocation().getUuid();
		labResult.setSynchronizedBy(defaultLocationUuid);
		labResultService.updateLabResult(labResult);
	}

	@Override
	public void loadEncounters(List<LabResult> labResults) {

		List<SyncLog> syncLogs = dao.getSyncLogsWithEncountersByLabResults(labResults);

		for (SyncLog syncLog : syncLogs) {
			for (LabResult labResult : labResults) {
				if (syncLog.belongsTo(labResult)) {
					labResult.setEncounterId(syncLog.getEncounter().getEncounterId());
				}
			}
		}
	}

	@Override
	public Long getSyncTaskRepeatInterval() {
		return dao.getSyncTaskRepeatInterval();
	}
}

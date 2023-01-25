package org.openmrs.module.disa.web.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ManageVLResultsDelegate {

	private LabResultService labResultService;

	private PatientService patientService;

	private LocationService locationService;

	@Autowired
	public ManageVLResultsDelegate(
			LabResultService labResultService,
			@Qualifier("patientService") PatientService patientService,
			@Qualifier("locationService") LocationService locationService) {
		this.labResultService = labResultService;
		this.patientService = patientService;
		this.locationService = locationService;
	}

	public List<Patient> findPatientsByDisa(Disa disa) {
		return patientService
				.getPatients(disa.getFirstName() + " " + disa.getLastName(), null, null,
						Boolean.FALSE);
	}

	public void doMapIdentifier(String patientUuid, Disa disa) throws DelegateException {

		if (!disa.getViralLoadStatus().equals("NOT_PROCESSED")
				|| !disa.getNotProcessingCause().equals("NID_NOT_FOUND")) {
			throw new DelegateException("The result to map is " + disa.getViralLoadStatus()
					+ ". It should be NOT_PROCESSED with cause NID_NOT_FOUND.");
		}

		Patient patient = patientService.getPatientByUuid(patientUuid);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		PatientIdentifierType identifierType = patientService
				.getPatientIdentifierTypeByUuid(Constants.DISA_NID);
		List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<PatientIdentifierType>();
		patientIdentifierTypes.add(identifierType);

		List<PatientIdentifier> patIdentidier = patientService.getPatientIdentifiers(disa.getNid(),
				patientIdentifierTypes, null, null, null);
		if (patIdentidier.isEmpty()) {

			// TODO handle network error!!!
			Disa updateDisa = new Disa();
			updateDisa.setRequestId(disa.getRequestId());
			updateDisa.setViralLoadStatus("PENDING");
			labResultService.updateLabResult(updateDisa);

			patientIdentifier.setPatient(patient);
			patientIdentifier.setIdentifier(disa.getNid());
			patientIdentifier.setIdentifierType(identifierType);
			patientIdentifier.setLocation(locationService.getDefaultLocation());
			patientService.savePatientIdentifier(patientIdentifier);
		}
	}

	public void addPatientToList(List<Patient> patients, Patient patient) {

		Patient patientToAdd = patientService.getPatient(patient.getId());
		if (!patients.contains(patientToAdd)) {
			// TODO This is a workaround to LazyInitialization error when getting
			// identifiers from patient on jsp
			Set<PatientIdentifier> identifiers = new TreeSet<PatientIdentifier>();
			identifiers.add(patientToAdd.getPatientIdentifier());
			patientToAdd.setIdentifiers(identifiers);
			patients.add(patientToAdd);
		}
	}
}

package org.openmrs.module.disa.web.delegate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.HttpResponseException;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.client.DisaAPIHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Component
public class ManageVLResultsDelegate {

	private static final Logger log = LoggerFactory.getLogger(ManageVLResultsDelegate.class);

	private DisaAPIHttpClient client;

	private PatientService patientService;

	private LocationService locationService;

	@Autowired
	public ManageVLResultsDelegate(
			DisaAPIHttpClient client,
			@Qualifier("patientService") PatientService patientService,
			@Qualifier("locationService") LocationService locationService) {
		this.client = client;
		this.patientService = patientService;
		this.locationService = locationService;
	}

	public OrgUnit getOrgUnit(String code) throws DelegateException {

		try {
			String response = client.getOrgUnit(code);
			return new Gson().fromJson(response, OrgUnit.class);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error fetching org unit {}: {}", code, message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

	public List<OrgUnit> searchOrgUnits(String term) throws DelegateException {

		try {
			String response = client.searchOrgUnits(term);
			TypeToken<List<OrgUnit>> listType = new TypeToken<List<OrgUnit>>() {
			};
			return new Gson().fromJson(response, listType.getType());
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error searching org units: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

	@Authorized({ "Gerir resultados no Disa Interoperabilidade" })
	public Disa getViralLoad(String requestId) throws DelegateException {
		try {
			log.info("Fetching Lab Result {}", requestId);
			String response = client.getViralLoad(requestId);
			return new Gson().fromJson(response, Disa.class);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error fetching Lab Result: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

	@Authorized({ "Gerir resultados no Disa Interoperabilidade" })
	public void deleteViralLoad(String requestId) throws DelegateException {
		try {
			log.info("Deleting Lab Result {}", requestId);
			client.deleteViralLoad(requestId);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error processing delete: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
	}

	@Authorized({ "Gerir resultados no Disa Interoperabilidade" })
	public Disa updateViralLoad(String requestId, Disa updates) throws DelegateException {
		try {
			log.info("Updating Lab Result {}", requestId);
			String response = client.updateViralLoad(requestId, new Gson().toJson(updates));
			return new Gson().fromJson(response, Disa.class);
		} catch (IOException | URISyntaxException e) {
			String message = e.getMessage();
			if (e instanceof HttpResponseException) {
				message = "" + ((HttpResponseException) e).getStatusCode();
			}
			log.error("Error processing update: {}", message);
			e.printStackTrace();
			throw new DelegateException("Unexpected error.");
		}
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
			updateDisa.setViralLoadStatus("PENDING");
			updateViralLoad(disa.getRequestId(), updateDisa);

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

package org.openmrs.module.disa.web.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/module/disa/managelabresults/{requestId}/map")
@SessionAttributes({ "requestId", "patientList", "lastSearchParams", "flashMessage", "errorSelectPatient" })
public class MapUnprocessedLabResultsController {

	private LabResultService labResultService;

	private PatientService patientService;

	private MessageSourceService messageSourceService;

	private DisaService disaService;

	@Autowired
	public MapUnprocessedLabResultsController(
			LabResultService labResultService,
			@Qualifier("patientService") PatientService patientService,
			MessageSourceService messageSourceService,
			DisaService disaService) {
		this.labResultService = labResultService;
		this.patientService = patientService;
		this.messageSourceService = messageSourceService;
		this.disaService = disaService;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String patientIdentifierMapping(
			@PathVariable String requestId,
			ModelMap model,
			HttpServletRequest request) {

		Disa disa = labResultService.getByRequestId(requestId);

		// If there isn't a requestId in the session or there is a different requestId,
		// load a new patient list.
		if (!model.containsAttribute("requestId")
				|| (!model.get("requestId").equals(requestId))) {
			model.addAttribute("requestId", requestId);
			model.addAttribute("patientList", disaService.getPatientsByDisa(disa));
		}

		// Build uri back to search results with used parameters.
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);
		if (model.containsAttribute("lastSearchParams")) {
			@SuppressWarnings("unchecked")
			MultiValueMap<String, String> params = (MultiValueMap<String, String>) model.get("lastSearchParams");
			builder.queryParams(params);
		}
		String searchUri = builder
				.pathSegment("module", "disa", "managelabresults.form")
				.build()
				.toUriString();

		model.addAttribute("lastSearchUri", searchUri);
		model.addAttribute(disa);

		return "/module/disa/managelabresults/map";
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public String mapPatientIdentifier(
			@PathVariable String requestId,
			@RequestParam(required = false) String patientUuid,
			ModelMap model) {

		Disa disa = labResultService.getByRequestId(requestId);

		if (patientUuid == null) {
			model.addAttribute(disa);
			model.addAttribute("errorSelectPatient", "disa.select.patient");
			return "/module/disa/managelabresults/map";
		} else {

			disaService.mapIdentifier(patientUuid, disa);
			String mapSuccessfulMsg = messageSourceService.getMessage("disa.viralload.map.successful", null,
					Context.getLocale());

			if (model.containsAttribute("lastSearchParams")) {
				@SuppressWarnings("unchecked")
				Map<String, String> lastSearchParams = (Map<String, String>) model.get("lastSearchParams");
				model.addAllAttributes(lastSearchParams);
			}

			// Remove requestId after successfully mapping, so that id does not filter
			// in managelabresults.
			model.remove("requestId");
			model.addAttribute("flashMessage", mapSuccessfulMsg);
		}

		return "redirect:/module/disa/managelabresults.form";
	}

	@RequestMapping(value = "/addPatient", method = RequestMethod.POST)
	public String addPatient(
			@PathVariable String requestId,
			@ModelAttribute("patient") Patient patient,
			@ModelAttribute("patientList") List<Patient> patients,
			ModelMap model) {

		if (patient.getId() == null) {
			model.addAttribute("errorPatientRequired", "disa.error.patient.required");
		} else {
			Patient patientToAdd = patientService.getPatient(patient.getId());
			if (!patients.contains(patientToAdd)) {
				// TODO This is a workaround to LazyInitialization error when getting
				// identifiers from patient on jsp
				Set<PatientIdentifier> identifiers = new TreeSet<>();
				identifiers.add(patientToAdd.getPatientIdentifier());
				patientToAdd.setIdentifiers(identifiers);
				patients.add(patientToAdd);
			}
			model.addAttribute("patientList", patients);
		}

		return "redirect:/module/disa/managelabresults/" + requestId + "/map.form";
	}

	@ModelAttribute("pageTitle")
	private void setPageTitle(ModelMap model) {
		String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
		String pageTitle = messageSourceService.getMessage("disa.map.identifiers", null, Context.getLocale());
		model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
	}
}

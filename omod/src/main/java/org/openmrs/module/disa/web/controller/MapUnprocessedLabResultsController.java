package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.ManageVLResultsDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
@SessionAttributes({ "requestId", "patientList", "lastSearchParams" })
public class MapUnprocessedLabResultsController {

	private ManageVLResultsDelegate manageVLResultsDelegate;

	private MessageSourceService messageSourceService;

	@Autowired
	public MapUnprocessedLabResultsController(ManageVLResultsDelegate manageVLResultsDelegate,
			MessageSourceService messageSourceService) {
		this.manageVLResultsDelegate = manageVLResultsDelegate;
		this.messageSourceService = messageSourceService;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {

		SimpleDateFormat dateFormat = Context.getDateFormat();
		// Allow converting empty String to null when binding Dates.
		// Without this validation will throw a typeMismatch error.
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String patientIdentifierMapping(
			@PathVariable String requestId,
			ModelMap model,
			HttpServletRequest request)
			throws DelegateException {

		Disa disa = manageVLResultsDelegate.getViralLoad(requestId);

		// If there isn't a requestId in the session or there is a different requestId,
		// load a new patient list.
		if (!model.containsAttribute("requestId")
				|| (!model.get("requestId").equals(requestId))) {
			model.addAttribute("requestId", requestId);
			model.addAttribute("patientList", manageVLResultsDelegate.findPatientsByDisa(disa));
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
			ModelMap model,
			RedirectAttributes redirectAttrs) throws DelegateException {

		Disa disa = manageVLResultsDelegate.getViralLoad(requestId);

		if (patientUuid == null) {
			model.addAttribute(disa);
			model.addAttribute("errorSelectPatient", "disa.select.patient");
			return "/module/disa/managelabresults/map";
		} else {

			manageVLResultsDelegate.doMapIdentifier(patientUuid, disa);
			String mapSuccessfulMsg = messageSourceService.getMessage("disa.viralload.map.successful", null,
					Context.getLocale());

			if (model.containsAttribute("lastSearchParams")) {
				@SuppressWarnings("unchecked")
				Map<String, String> lastSearchParams = (Map<String, String>) model.get("lastSearchParams");
				redirectAttrs.addAllAttributes(lastSearchParams);
			}

			redirectAttrs.addFlashAttribute("flashMessage", mapSuccessfulMsg);
		}

		return "redirect:/module/disa/managelabresults.form";
	}

	@RequestMapping(value = "/addPatient", method = RequestMethod.POST)
	public String addPatient(
			@PathVariable String requestId,
			@ModelAttribute("patient") Patient patient,
			@ModelAttribute("patientList") List<Patient> patients,
			ModelMap model,
			RedirectAttributes redirectAttrs) {

		if (patient.getId() == null) {
			redirectAttrs.addFlashAttribute("errorPatientRequired", "disa.error.patient.required");
		} else {
			manageVLResultsDelegate.addPatientToList(patients, patient);
			model.addAttribute("patientList", patients);
		}

		return "redirect:/module/disa/" + requestId + "/map.form";
	}

	@ModelAttribute("pageTitle")
	private void setPageTitle(ModelMap model) {
		String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
		String pageTitle = messageSourceService.getMessage("disa.map.identifiers", null, Context.getLocale());
		model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
	}
}

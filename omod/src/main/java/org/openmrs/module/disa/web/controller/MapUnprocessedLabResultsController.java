package org.openmrs.module.disa.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/module/disa/managelabresults/{id}/map")
@SessionAttributes({ "flashMessage" })
public class MapUnprocessedLabResultsController {

	private LabResultService labResultService;

	private MessageSourceService messageSourceService;

	private DisaService disaService;

	@Autowired
	public MapUnprocessedLabResultsController(
			LabResultService labResultService,
			MessageSourceService messageSourceService,
			DisaService disaService) {
		this.labResultService = labResultService;
		this.messageSourceService = messageSourceService;
		this.disaService = disaService;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String patientIdentifierMapping(
			@PathVariable long id,
			ModelMap model,
			HttpSession session,
			HttpServletRequest request) {

		LabResult labResult = labResultService.getById(id);

		// Load suggestions
		model.addAttribute("searchSuggestion", labResult.getFirstName() + " " + labResult.getLastName());

		// Build uri back to search results with used parameters.
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);
		if (session.getAttribute("lastSearchParams") != null) {
			@SuppressWarnings("unchecked")
			MultiValueMap<String, String> params = (MultiValueMap<String, String>) session
					.getAttribute("lastSearchParams");
			builder.queryParams(params);
		}
		String searchUri = builder
				.pathSegment("module", "disa", "managelabresults.form")
				.build()
				.toUriString();

		model.addAttribute("lastSearchUri", searchUri);
		model.addAttribute("labResult", labResult);

		return "/module/disa/managelabresults/map";
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public String mapPatientIdentifier(
			@PathVariable long id,
			@RequestParam(required = false) String patientUuid,
			@RequestParam(required = false) String search,
			ModelMap model) {

		LabResult labResult = labResultService.getById(id);

		try {

			if (patientUuid == null) {
				throw new DisaModuleAPIException("disa.select.patient", new Object[] {});

			}

			Patient mapped = disaService.mapIdentifier(patientUuid, labResult);
			// If successfully mapped, we can trust that the mapped indentifier is not null.
			String[] args = new String[] { labResult.getNid(), mapped.getPatientIdentifier().getIdentifier() };
			String mapSuccessfulMsg = messageSourceService.getMessage("disa.viralload.map.successful", args,
					Context.getLocale());

			if (model.containsAttribute("lastSearchParams")) {
				@SuppressWarnings("unchecked")
				Map<String, String> lastSearchParams = (Map<String, String>) model.get("lastSearchParams");
				model.addAllAttributes(lastSearchParams);
			}

			model.addAttribute("flashMessage", mapSuccessfulMsg);

			return "redirect:/module/disa/managelabresults.form";

		} catch (DisaModuleAPIException e) {
			model.addAttribute("labResult", labResult);
			model.addAttribute("flashMessage", e.getLocalizedMessage());
			// Load suggestions
			model.addAttribute("searchSuggestion", search);
			return "/module/disa/managelabresults/map";
		}

	}

	@ModelAttribute("pageTitle")
	private void setPageTitle(ModelMap model) {
		String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
		String pageTitle = messageSourceService.getMessage("disa.map.identifiers", null, Context.getLocale());
		model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
	}
}

package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class SearchLabResultsController {

	private MessageSourceService messageSourceService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {

		SimpleDateFormat dateFormat = Context.getDateFormat();
		// Allow converting empty String to null when binding Dates.
		// Without this validation will throw a typeMismatch error.
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	@Autowired
	public void setMessageSourceService(MessageSourceService messageSourceService) {
		this.messageSourceService = messageSourceService;
	}

	@RequestMapping(value = "/module/disa/searchlabresults/viralLoadStagingServer", method = RequestMethod.GET)
	public String showViralLoadStagingQueryForm(
			@RequestParam MultiValueMap<String, String> params,
			@Valid SearchForm searchForm,
			BindingResult result,
			ModelMap model,
			HttpSession session,
			HttpServletRequest request) throws Exception {

		populateSismaCodes(model);

		if (params.isEmpty()) {
			model.addAttribute(new SearchForm());
		} else if (result.hasErrors()) {
			model.addAttribute(searchForm);
		} else {
			String exportUri = ServletUriComponentsBuilder.fromServletMapping(request)
					.queryParams(params)
					.pathSegment("module", "disa", "searchlabresults", "export.form")
					.build()
					.toUriString();
			model.addAttribute("exportUri", exportUri);

			ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
			model.addAttribute(delegate.getViralLoadDataList(searchForm));
			session.setAttribute("lastSearchParams", params);
		}

		return "/module/disa/searchlabresults/viralLoadStagingServer";
	}

	@RequestMapping(value = "/module/disa/searchlabresults/export", method = RequestMethod.GET)
	public void export(
			@RequestParam MultiValueMap<String, String> params,
			@Valid SearchForm searchForm,
			BindingResult result,
			ModelMap model,
			HttpServletResponse response) throws Exception {

		ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
		delegate.createExcelFileStaging(delegate.getViralLoadDataList(searchForm), response, messageSourceService);
	}

	/**
	 * Populates SISMA code dropdown options.
	 */
	private void populateSismaCodes(ModelMap model) {
		String propertyValue = Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue();
		List<String> sismaCodes = Arrays.asList(propertyValue.split(","));
		List<String> sismaCodesTodos = new ArrayList<String>();

		sismaCodesTodos.add(Constants.TODOS);
		sismaCodesTodos.addAll(sismaCodes);

		model.addAttribute("sismaCodes", sismaCodesTodos);
	}
}

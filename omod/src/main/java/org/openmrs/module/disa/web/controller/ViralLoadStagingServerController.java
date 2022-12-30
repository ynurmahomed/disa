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
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViralLoadStagingServerController {

	private ViralLoadResultsDelegate delegate;

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

	@RequestMapping(value = "/module/disa/viralLoadStagingServer", method = RequestMethod.GET)
	public SearchForm showViralLoadStagingQueryForm(ModelMap model, HttpSession session) {
		delegate = new ViralLoadResultsDelegate();
		model.addAttribute("user", Context.getAuthenticatedUser());
		return new SearchForm();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/module/disa/viralLoadStagingServerResult", method = RequestMethod.POST)
	public void downloadExcelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Disa> vlDataLst = (List<Disa>) request.getSession().getAttribute("vlDataLst");
		delegate.createExcelFileStaging(vlDataLst, response, messageSourceService);
	}

	@RequestMapping(value = "/module/disa/viralLoadStagingServer", method = RequestMethod.POST)
	public String showViralLoadList(
			@Valid SearchForm searchForm,
			BindingResult result,
			HttpServletRequest request,
			HttpSession session) throws Exception {

		if (result.hasErrors()) {
			return "/module/disa/viralLoadStagingServer";
		}

		session.setAttribute("requestId", searchForm.getRequestId());
		session.setAttribute("nid", searchForm.getNid());
		session.setAttribute("vlSisma", searchForm.getVlSisma());
		session.setAttribute("referringId", searchForm.getReferringId());
		session.setAttribute("vlState", searchForm.getVlState());
		session.setAttribute("startDate", searchForm.getStartDate());
		session.setAttribute("endDate", searchForm.getEndDate());

		return "redirect:viralLoadStagingServerResult.form";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/module/disa/viralLoadStagingServerResult", method = RequestMethod.GET)
	public void showViralLoadResultList(HttpServletRequest request, HttpSession session, ModelMap model,
			@RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) throws Exception {

		String requestId = (String) session.getAttribute("requestId");

		String nid = (String) session.getAttribute("nid");

		String vlSisma = (String) session.getAttribute("vlSisma");

		String referringId = (String) session.getAttribute("referringId");

		String vlState = (String) session.getAttribute("vlState");

		Date startDate = (Date) session.getAttribute("startDate");

		Date endDate = (Date) session.getAttribute("endDate");
		
		List<String> healthFacCodes = (List<String>) session.getAttribute("hFCodes");

		List<Disa> vlDataLst = delegate.getViralLoadDataList(requestId, nid, vlSisma, referringId, vlState, startDate,
				endDate, healthFacCodes);
		session.setAttribute("vlDataLst", vlDataLst);
		session.setAttribute("openmrs_msg", openmrs_msg);
	}

	/**
	 * Populates SISMA code dropdown options.
	 */
	@ModelAttribute
	private void populateSismaCodes(HttpSession session, ModelMap model) {
		String propertyValue = Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue();
		List<String> sismaCodes = Arrays.asList(propertyValue.split(","));
		List<String> sismaCodesTodos = new ArrayList<String>();

		sismaCodesTodos.add(Constants.TODOS);
		sismaCodesTodos.addAll(sismaCodes);

		session.setAttribute("hFCodes", sismaCodes);  
		model.addAttribute("sismaCodes", sismaCodesTodos);
	}
}
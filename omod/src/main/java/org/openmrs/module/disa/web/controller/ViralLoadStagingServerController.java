package org.openmrs.module.disa.web.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ViralLoadStagingServerController {
	
	private ViralLoadResultsDelegate delegate;
	
	public MessageSourceService messageSourceService;

	@Autowired
	public void setMessageSourceService(MessageSourceService messageSourceService) {
		this.messageSourceService = messageSourceService;
	}
	
	@RequestMapping(value = "/module/disa/viralLoadStagingServer", method = RequestMethod.GET)
	public void showViralLoadStagingQueryForm(ModelMap model, HttpSession session) {
		List<String> sismaCodes = Arrays.asList(Context.getAdministrationService()
				.getGlobalPropertyObject(Constants.DISA_SISMA_CODE).getPropertyValue().split(","));
		delegate = new ViralLoadResultsDelegate();
		model.addAttribute("user", Context.getAuthenticatedUser());
		session.setAttribute("sismaCodes", sismaCodes);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/module/disa/viralLoadStagingServerResult", method = RequestMethod.POST)
	public void downloadExcelFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Disa> vlDataLst = (List<Disa>) request.getSession().getAttribute("vlDataLst");
		delegate.createExcelFileStaging(vlDataLst, response, messageSourceService);
	}
	
	@RequestMapping(value = "/module/disa/viralLoadStagingServer", method = RequestMethod.POST)
	public ModelAndView showViralLoadList(HttpServletRequest request, HttpSession session, 
			@RequestParam("requestId") String requestId, @RequestParam("nid") String nid,
			@RequestParam("vlSisma") String vlSisma, @RequestParam("referringId") String referringId,
			@RequestParam("vlState") String vlState, @RequestParam("startDate") Date startDate,
			@RequestParam("endDate") Date endDate) throws Exception {
		ModelAndView model = new ModelAndView();

		if (startDate == null) {
			model.addObject("errorStartDateRequired", "disa.error.startDate");
		}
		if (endDate == null) {
			model.addObject("errorEndDateRequired", "disa.error.endDate");
		}

		if (startDate == null || endDate == null) {
			return model;
		}

		session.setAttribute("requestId", requestId);
		session.setAttribute("nid", nid);
		session.setAttribute("vlSisma", vlSisma);
		session.setAttribute("referringId", referringId);
		session.setAttribute("vlState", vlState);
		session.setAttribute("startDate", startDate);
		session.setAttribute("endDate", endDate);

		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadStagingServerResult.form"));
	}
	
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

		List<Disa> vlDataLst = delegate.getViralLoadDataList(requestId, nid, vlSisma, referringId, vlState, startDate, endDate);
		session.setAttribute("vlDataLst", vlDataLst);
		session.setAttribute("openmrs_msg", openmrs_msg);
	}
}
package org.openmrs.module.disa.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
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
	public void showViralLoadStagingQueryForm(ModelMap model) {
		delegate = new ViralLoadResultsDelegate();
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(value = "/module/disa/viralLoadStagingServer", method = RequestMethod.POST)
	public ModelAndView showViralLoadList(HttpServletRequest request, HttpSession session, 
			@RequestParam("requestId") String requestId) throws Exception {
		ModelAndView model = new ModelAndView();

		if (requestId.isEmpty()) {
			model.addObject("errorRequestIdRequired", "disa.error.requestId");
		}

		if (requestId.isEmpty()) {
			return model;
		}

		session.setAttribute("requestId", requestId);

		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadStagingServerResult.form"));
	}
	
	@RequestMapping(value = "/module/disa/viralLoadStagingServerResult", method = RequestMethod.GET)
	public void showViralLoadResultList(HttpServletRequest request, HttpSession session, ModelMap model,
			@RequestParam(required = false, value = "openmrs_msg") String openmrs_msg) throws Exception {

		String requestId = (String) session.getAttribute("requestId");

		List<Disa> vlDataLst = delegate.getViralLoadDataList(requestId);
		session.setAttribute("vlDataLst", vlDataLst);
		session.setAttribute("openmrs_msg", openmrs_msg);
	}
}

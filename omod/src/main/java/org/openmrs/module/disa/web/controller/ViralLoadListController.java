package org.openmrs.module.disa.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ViralLoadListController {

	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/disa/viralLoadList", method = RequestMethod.GET)
	public void showViralLoadList(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
}

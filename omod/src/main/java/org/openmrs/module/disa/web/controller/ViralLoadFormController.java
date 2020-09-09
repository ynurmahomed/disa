/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.disa.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.extension.util.RestUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class  ViralLoadFormController {
	
	private RestUtil rest;
	
	protected final Log log = LogFactory.getLog(getClass());
		
	@RequestMapping(value = "/module/disa/viralLoadForm", method = RequestMethod.GET)
	public void showViralLoadForm(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
	@RequestMapping(value = "/module/disa/viralLoadForm", method = RequestMethod.POST)
	public ModelAndView processForm(HttpServletRequest request, @RequestParam("vlState") String state) throws Exception {
		
		rest = new RestUtil();
		rest.setURLBase(Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());
		
		List<LocationAttribute> loAttribute = new ArrayList<LocationAttribute>(Context.getLocationService().getDefaultLocation().getAttributes());
		
		List<String> sismaCodes = new ArrayList<String>();
		sismaCodes.add(loAttribute.get(0).getValueReference());
		
		String jsonViralLoadInfo = rest.getRequestGetFsrByStatus("/viral-status", 
				new ArrayList<String>(Arrays.asList(loAttribute.get(0).getValueReference())), request.getParameter("vlState"));
		List<Disa> vlDataLst = new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {}.getType());
		
		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("vlDataLst", vlDataLst);
		
		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadList.form"));   
	}
}

package org.openmrs.module.disa.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpRequest;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.api.PersonService;
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
public class ViralLoadResultsController {

	private RestUtil rest;

	@RequestMapping(value = "/module/disa/viralLoadStatusList", method = RequestMethod.GET)
	public void showViralLoadStatusList(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}

	@RequestMapping(value = "/module/disa/viralLoadStatusList", method = RequestMethod.POST)
	public ModelAndView showViralLoadList(HttpServletRequest request, @RequestParam("vlState") String state)
			throws Exception {

		rest = new RestUtil();
		rest.setURLBase(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_URL).getPropertyValue());
		rest.setUsername(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_USERNAME).getPropertyValue());
		rest.setPassword(
				Context.getAdministrationService().getGlobalPropertyObject(Constants.DISA_PASSWORD).getPropertyValue());

		List<LocationAttribute> loAttribute = new ArrayList<LocationAttribute>(
				Context.getLocationService().getDefaultLocation().getAttributes());

		List<String> sismaCodes = new ArrayList<String>();
		sismaCodes.add(loAttribute.get(0).getValueReference());

		String jsonViralLoadInfo = rest.getRequestGetFsrByStatus("/viral-status",
				new ArrayList<String>(Arrays.asList(loAttribute.get(0).getValueReference())),
				request.getParameter("vlState"));
		List<Disa> vlDataLst = new Gson().fromJson(jsonViralLoadInfo, new TypeToken<ArrayList<Disa>>() {
		}.getType());

		HttpSession httpSession = request.getSession();
		httpSession.setAttribute("vlDataLst", vlDataLst);

		return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadResultsList.form"));
	}

	@RequestMapping(value = "/module/disa/viralLoadResultsList", method = RequestMethod.GET)
	public void showViralLoadResultList(ModelMap model) {
	}

	@RequestMapping(value = "/module/disa/mapPatientIdentifierForm", method = RequestMethod.GET)
	public void patientIdentifierMapping(HttpSession session, HttpServletRequest request) {
		String name = (String) request.getParameter("name");
		String nid = (String) request.getParameter("nid");

		List<Disa> vlDataLst = (List<Disa>) session.getAttribute("vlDataLst");
		Disa selectedPatient = null;
		for (Disa disa : vlDataLst) {
			if (disa.getNid().equals(nid)) {
				selectedPatient = disa;
				break;
			}
		}

		System.out.println(nid);

		// FIXME after sorting out fields that are coming null
		List<Patient> matchingPatients = Context.getPatientService().getPatientsByName("diolinda");//.getPatientsByName(name);

		session.setAttribute("selectedPatient", selectedPatient);
		session.setAttribute("patients", matchingPatients);
	}

	@RequestMapping(value = "/module/disa/mapPatientIdentifierForm", method = RequestMethod.POST)
	public void mapPatientIdentifier(ModelMap model) {

	}

}

package org.openmrs.module.disa.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class EditNotProcessedController {

	protected final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(value = "/module/disa/editNotProcessedForm", method = RequestMethod.GET)
	public void showEditNotProcessedForm(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());		
	}
	
	@RequestMapping(value = "/module/disa/editNotProcessedForm", method = RequestMethod.POST)
	public ModelAndView submitForm(HttpServletRequest request, @RequestParam("idPatient") String idPatient, @RequestParam("nid") String nidDisa) throws Exception {
		if(idPatient == null || idPatient == "") {
			return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/editNotProcessedForm.form"));
		}else {
					
			Patient patient = Context.getPatientService().getPatient(Integer.parseInt(idPatient));
			PatientIdentifier patientIdentifier = new PatientIdentifier();
			PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierType(15);
			List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<PatientIdentifierType>();
			patientIdentifierTypes.add(identifierType);
			
			List<PatientIdentifier> patIdentidier = Context.getPatientService().getPatientIdentifiers(nidDisa, patientIdentifierTypes, null, null, null);
				if(patIdentidier.isEmpty()) {
					patientIdentifier.setPatient(patient);
					patientIdentifier.setIdentifier(nidDisa);
					patientIdentifier.setIdentifierType(identifierType);
					patientIdentifier.setLocation(Context.getLocationService().getDefaultLocation());
					Context.getPatientService().savePatientIdentifier(patientIdentifier);
					
					//post
					
					
					return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadForm.form"));
				}else {
				return new ModelAndView(new RedirectView(request.getContextPath() + "/module/disa/viralLoadForm.form"));
			}				
		}
	}
}

package org.openmrs.module.disa.web.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.ManageVLResultsDelegate;
import org.openmrs.module.disa.web.model.ReallocateForm;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/module/disa/managelabresults/{requestId}/reallocate")
public class ReallocateLabResultsController {

    private ManageVLResultsDelegate manageVLResultsDelegate;

    private MessageSourceService messageSourceService;

    @Autowired
    public ReallocateLabResultsController(ManageVLResultsDelegate manageVLResultsDelegate,
            MessageSourceService messageSourceService) {
        this.manageVLResultsDelegate = manageVLResultsDelegate;
        this.messageSourceService = messageSourceService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String reallocateForm(
            @PathVariable String requestId,
            ModelMap model,
            SearchForm searchForm,
            HttpSession session) throws DelegateException {

        Disa vl = this.manageVLResultsDelegate.getViralLoad(requestId);
        OrgUnit orgUnit = this.manageVLResultsDelegate.getOrgUnit(vl.getHealthFacilityLabCode());

        model.addAttribute(new ReallocateForm());
        model.addAttribute(orgUnit);

        return "/module/disa/managelabresults/reallocate";
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public String reallocate(@PathVariable String requestId,
            @Valid @ModelAttribute ReallocateForm reallocateForm,
            BindingResult result,
            ModelMap model,
            HttpSession session,
            RedirectAttributes redirectAttrs) throws DelegateException {

        if (result.hasErrors()) {
            Disa vl = this.manageVLResultsDelegate.getViralLoad(requestId);
            OrgUnit orgUnit = this.manageVLResultsDelegate.getOrgUnit(vl.getHealthFacilityLabCode());
            model.addAttribute(reallocateForm);
            model.addAttribute(orgUnit);
            return "/module/disa/managelabresults/reallocate";
        }

        OrgUnit orgUnit = this.manageVLResultsDelegate.getOrgUnit(reallocateForm.getHealthFacilityLabCode());

        Disa update = new Disa();
        update.setHealthFacilityLabCode(orgUnit.getCode());
        update.setRequestingFacilityName(orgUnit.getFacility());
        update.setRequestingDistrictName(orgUnit.getDistrict());
        update.setRequestingProvinceName(orgUnit.getProvince());
        update.setViralLoadStatus("PENDING");

        this.manageVLResultsDelegate.updateViralLoad(requestId, update);

        @SuppressWarnings("unchecked")
        Map<String, String> params = ((Map<String, String>) session.getAttribute("lastSearchParams"));
        redirectAttrs.addAllAttributes(params);
        redirectAttrs.addFlashAttribute("flashMessage", getReallocatedMessage(requestId, update));
        return "redirect:/module/disa/managelabresults.form";
    }

    private String getReallocatedMessage(String requestId, Disa update) {
        Object[] args = new Object[] { requestId, update.getRequestingFacilityName(),
                update.getHealthFacilityLabCode() };
        return messageSourceService.getMessage("disa.viralload.reallocate.successful", args, Context.getLocale());
    }

    @ModelAttribute("pageTitle")
	private void setPageTitle(ModelMap model) {
		String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
		String pageTitle = messageSourceService.getMessage("disa.viralload.reallocate.title", null, Context.getLocale());
		model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
	}
}

package org.openmrs.module.disa.web.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.web.model.ReallocateForm;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/module/disa/managelabresults/{id}/reallocate")
@SessionAttributes({ "flashMessage" })
public class ReallocateLabResultsController {

    private OrgUnitService orgUnitService;

    private LabResultService labResultService;

    private MessageSourceService messageSourceService;

    @Autowired
    public ReallocateLabResultsController(
            OrgUnitService orgUnitService,
            LabResultService labResultService,
            MessageSourceService messageSourceService) {

        this.orgUnitService = orgUnitService;
        this.labResultService = labResultService;
        this.messageSourceService = messageSourceService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String reallocateForm(
            @PathVariable long id,
            ModelMap model,
            SearchForm searchForm,
            HttpSession session) {

        LabResult labResult = labResultService.getById(id);
        OrgUnit orgUnit = orgUnitService.getOrgUnitByCode(labResult.getHealthFacilityLabCode());

        model.addAttribute(new ReallocateForm());
        model.addAttribute(orgUnit);
        model.addAttribute("requestId", labResult.getRequestId());

        return "/module/disa/managelabresults/reallocate";
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public String reallocate(@PathVariable long id,
            @Valid @ModelAttribute ReallocateForm reallocateForm,
            BindingResult result,
            ModelMap model,
            HttpSession session) {

        // Because @Valid is not yet supported by controller in OpenMRS
        // we have to manually validate the form :/
        if (StringUtils.isEmpty(reallocateForm.getHealthFacilityLabCode())) {
            result.rejectValue("healthFacilityLabCode", "NotBlank.reallocateForm.healthFacilityLabCode");
        }

        if (result.hasErrors()) {
            LabResult labResult = labResultService.getById(id);
            OrgUnit orgUnit = orgUnitService.getOrgUnitByCode(labResult.getHealthFacilityLabCode());
            model.addAttribute(reallocateForm);
            model.addAttribute(orgUnit);
            model.addAttribute("requestId", labResult.getRequestId());
            return "/module/disa/managelabresults/reallocate";
        }

        OrgUnit destination = new OrgUnit(reallocateForm.getHealthFacilityLabCode());
        LabResult update = labResultService.reallocateLabResult(id, destination);

        @SuppressWarnings("unchecked")
        Map<String, String> params = ((Map<String, String>) session.getAttribute("lastSearchParams"));
        model.addAllAttributes(params);
        model.addAttribute("flashMessage", getReallocatedMessage(update));
        return "redirect:/module/disa/managelabresults.form";
    }

    private String getReallocatedMessage(LabResult labResult) {
        // TODO check if reallocation message still makes sence
        Object[] args = new Object[] { labResult.getRequestId(), labResult.getRequestingFacilityName(),
                labResult.getHealthFacilityLabCode() };
        return messageSourceService.getMessage("disa.viralload.reallocate.successful", args, Context.getLocale());
    }

    @ModelAttribute("pageTitle")
    private void setPageTitle(ModelMap model) {
        String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
        String pageTitle = messageSourceService.getMessage("disa.viralload.reallocate.title", null,
                Context.getLocale());
        model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
    }
}

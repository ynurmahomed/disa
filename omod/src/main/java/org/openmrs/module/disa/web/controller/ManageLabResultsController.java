package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.ManageVLResultsDelegate;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.ReallocateForm;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping(value = "/module/disa/managelabresults")
public class ManageLabResultsController {

    private ManageVLResultsDelegate manageVLResultsDelegate;

    private MessageSourceService messageSourceService;

    @Autowired
    public ManageLabResultsController(ManageVLResultsDelegate manageVLResultsDelegate,
            MessageSourceService messageSourceService) {
        this.manageVLResultsDelegate = manageVLResultsDelegate;
        this.messageSourceService = messageSourceService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = Context.getDateFormat();
        // true passed to CustomDateEditor constructor means convert empty String to
        // null
        // TODO configure this editor globally
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String search(
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
                    .pathSegment("module", "disa", "managelabresults", "export.form")
                    .build()
                    .toUriString();
            model.addAttribute("exportUri", exportUri);

            ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
            model.addAttribute(delegate.getViralLoadDataList(searchForm));
            session.setAttribute("lastSearchParams", params);
        }

        return "/module/disa/managelabresults/index";
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(
            @RequestParam MultiValueMap<String, String> params,
            @Valid SearchForm searchForm,
            BindingResult result,
            ModelMap model,
            HttpServletResponse response) throws Exception {

        ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
        delegate.createExcelFileStaging(delegate.getViralLoadDataList(searchForm), response, messageSourceService);
    }

    @RequestMapping(value = "/{requestId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String requestId) throws DelegateException {
        this.manageVLResultsDelegate.deleteViralLoad(requestId);
    }

    @RequestMapping(value = "/{requestId}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable String requestId, @RequestBody Disa disa) throws DelegateException {
        this.manageVLResultsDelegate.updateViralLoad(requestId, disa);
    }

    @RequestMapping(value = "/{requestId}/reallocate", method = RequestMethod.GET)
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

    @RequestMapping(value = "/{requestId}/reallocate", method = RequestMethod.POST)
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

    @RequestMapping(value = "/orgunits/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<OrgUnit> searchOrgUnits(@RequestParam String term, Model model) throws DelegateException {
        return this.manageVLResultsDelegate.searchOrgUnits(term);
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

    private String getReallocatedMessage(String requestId, Disa update) {
        Object[] args = new Object[] { requestId, update.getRequestingFacilityName(),
                update.getHealthFacilityLabCode() };
        return messageSourceService.getMessage("disa.viralload.reallocate.successful", args, Context.getLocale());
    }
}

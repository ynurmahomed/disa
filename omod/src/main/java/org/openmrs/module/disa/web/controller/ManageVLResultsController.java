package org.openmrs.module.disa.web.controller;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.OrgUnitDelegate;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/module/disa/managevlresults")
public class ManageVLResultsController {

    @Autowired
    private OrgUnitDelegate orgUnitDelegate;

    @InitBinder
    public void initBinder(WebDataBinder binder) {

        SimpleDateFormat dateFormat = Context.getDateFormat();
        // true passed to CustomDateEditor constructor means convert empty String to
        // null
        // otherwise validation will throw a typeMismatch error.
        // TODO configure this editor globally
        // https://docs.spring.io/spring-framework/docs/4.1.4.RELEASE/spring-framework-reference/html/validation.html#beans-beans-conversion-customeditor-registration
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ModelAndView search(
            @RequestParam MultiValueMap<String, String> params,
            @Valid SearchForm searchForm,
            BindingResult result,
            ModelMap model,
            HttpSession session) throws Exception {

        ModelAndView mav = new ModelAndView();

        if (params.isEmpty()) {
            mav.setViewName("/module/disa/managevlresults/search");
            populateSismaCodes(model);
            mav.addObject(new SearchForm());
        } else if (result.hasErrors()) {
            mav.setViewName("/module/disa/managevlresults/search");
            populateSismaCodes(model);
            mav.addObject(searchForm);
        } else {
            ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
            mav.setViewName("/module/disa/managevlresults/searchResults");
            mav.addObject(delegate.getViralLoadDataList(searchForm));
            session.setAttribute("lastSearchParams", params);
        }

        return mav;
    }

    @RequestMapping(value = "/{requestId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String requestId) throws DelegateException {
        new ViralLoadResultsDelegate().deleteLabResult(requestId);
    }

    @RequestMapping(value = "/{requestId}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable String requestId, @RequestBody Disa disa) throws DelegateException {
        new ViralLoadResultsDelegate().updateLabResult(requestId, disa);
    }

    @RequestMapping(value = "/{requestId}/reallocate", method = RequestMethod.GET)
    public ModelAndView reallocateForm(@PathVariable String requestId,
            SearchForm searchForm,
            HttpSession session) throws DelegateException {

        ModelAndView mav = new ModelAndView();

        Disa vl = new ViralLoadResultsDelegate().getViralLoadData(requestId);
        OrgUnit orgUnit = this.orgUnitDelegate.getOrgUnit(vl.getHealthFacilityLabCode());

        mav.setViewName("/module/disa/managevlresults/reallocate");
        mav.addObject(new ReallocateForm());
        mav.addObject(orgUnit);
        return mav;
    }

    @RequestMapping(value = "/{requestId}/reallocate", method = RequestMethod.POST)
    public ModelAndView reallocate(@PathVariable String requestId,
            @Valid @ModelAttribute ReallocateForm reallocateForm,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttrs) throws DelegateException {

        ModelAndView mav = new ModelAndView();

        if (result.hasErrors()) {
            Disa vl = new ViralLoadResultsDelegate().getViralLoadData(requestId);
            OrgUnit orgUnit = this.orgUnitDelegate.getOrgUnit(vl.getHealthFacilityLabCode());
            mav.setViewName("/module/disa/managevlresults/reallocate");
            mav.addObject(reallocateForm);
            mav.addObject(orgUnit);
            return mav;
        }

        OrgUnit orgUnit = this.orgUnitDelegate.getOrgUnit(reallocateForm.getHealthFacilityLabCode());

        Disa update = new Disa();
        update.setHealthFacilityLabCode(orgUnit.getCode());
        update.setRequestingFacilityName(orgUnit.getFacility());
        update.setRequestingDistrictName(orgUnit.getDistrict());
        update.setRequestingProvinceName(orgUnit.getProvince());

        new ViralLoadResultsDelegate().updateLabResult(requestId, update);

        @SuppressWarnings("unchecked")
        Map<String, String> params = ((Map<String, String>) session.getAttribute("lastSearchParams"));
        redirectAttrs.addAllAttributes(params);
        redirectAttrs.addFlashAttribute("flashMessage", "disa.viralload.reallocate.successful");
        mav.setViewName("redirect:/module/disa/managevlresults/search.form");
        return mav;
    }

    @RequestMapping(value = "/orgunits/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<OrgUnit> searchOrgUnits(@RequestParam String term, Model model) throws DelegateException {
        return this.orgUnitDelegate.search(term);
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
}

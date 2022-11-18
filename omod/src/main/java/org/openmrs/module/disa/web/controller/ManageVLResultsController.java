package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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

@Controller
@RequestMapping(value = "/module/disa/managevlresults")
public class ManageVLResultsController {

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
            @RequestParam Map<String, String> params,
            @Valid SearchForm searchForm,
            BindingResult result) throws Exception {

        ModelAndView mav = new ModelAndView();

        if (params.isEmpty()) {
            mav.setViewName("/module/disa/managevlresults/search");
            mav.addObject(new SearchForm());

        } else if (result.hasErrors()) {
            mav.setViewName("/module/disa/managevlresults/search");
            mav.addObject(searchForm);
        } else {
            ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
            mav.setViewName("/module/disa/managevlresults/searchResults");
            mav.addObject(delegate.getViralLoadDataList(searchForm));
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

    /**
     * Populates SISMA code dropdown options.
     */
    @ModelAttribute
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

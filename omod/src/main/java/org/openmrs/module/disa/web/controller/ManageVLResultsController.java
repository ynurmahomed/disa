package org.openmrs.module.disa.web.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.extension.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/module/disa/manageVLResults")
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

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public SearchForm search(ModelMap model) {
        return new SearchForm();
    }

    @RequestMapping(value = "search", method = RequestMethod.POST)
    public String search(@Valid SearchForm searchForm,
            BindingResult result,
            RedirectAttributes redir) throws Exception {

        if (result.hasErrors()) {
            return "/module/disa/manageVLResults/search";
        }

        ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
        List<Disa> vlDataLst = delegate.getViralLoadDataList(searchForm);

        redir.addFlashAttribute("vlDataLst", vlDataLst);

        return "redirect:searchResults.form";
    }

    @RequestMapping(value = "searchResults", method = RequestMethod.GET)
    public void searchResults() {
        // TODO document why this method is empty
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

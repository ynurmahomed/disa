package org.openmrs.module.disa.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.DisaModuleAPIException;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/module/disa/managelabresults")
public class ManageLabResultsController {

    private static final Logger log = LoggerFactory.getLogger(ManageLabResultsController.class);

    private LabResultService labResultService;

    private MessageSourceService messageSourceService;

    @Autowired
    public ManageLabResultsController(
            LabResultService labResultService,
            MessageSourceService messageSourceService) {
        this.labResultService = labResultService;
        this.messageSourceService = messageSourceService;
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
    public void delete(@PathVariable String requestId) {
        labResultService.deleteByRequestId(requestId);
    }

    @RequestMapping(value = "/{requestId}/reschedule", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void reschedule(@PathVariable String requestId) {
        labResultService.rescheduleLabResult(new Disa(requestId));
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

    @ModelAttribute("pageTitle")
    private void setPageTitle(ModelMap model) {
        String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
        String pageTitle = messageSourceService.getMessage("disa.list.viral.load.results.manage", null,
                Context.getLocale());
        model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
    }

    @ExceptionHandler(DisaModuleAPIException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleDisaModuleAPIException(DisaModuleAPIException e) {
        log.error("", e);
    }
}

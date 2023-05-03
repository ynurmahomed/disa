package org.openmrs.module.disa.web.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.web.delegate.ViralLoadResultsDelegate;
import org.openmrs.module.disa.web.model.SearchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequestMapping("/module/disa/managelabresults")
@SessionAttributes({ "flashMessage" })
public class ManageLabResultsController {

    private static final Logger log = LoggerFactory.getLogger(ManageLabResultsController.class);

    private LabResultService labResultService;

    private MessageSourceService messageSourceService;

    private AdministrationService administrationService;

    @Autowired
    public ManageLabResultsController(
            LabResultService labResultService,
            MessageSourceService messageSourceService,
            @Qualifier("adminService") AdministrationService administrationService) {
        this.labResultService = labResultService;
        this.messageSourceService = messageSourceService;
        this.administrationService = administrationService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String search(
            @RequestParam MultiValueMap<String, String> params,
            @Valid SearchForm searchForm,
            BindingResult result,
            ModelMap model,
            HttpSession session,
            HttpServletRequest request) {

        populateSismaCodes(model);

        if (result.hasErrors()) {
            model.addAttribute(searchForm);
        } else {
            String exportUri = ServletUriComponentsBuilder.fromServletMapping(request)
                    .queryParams(params)
                    .pathSegment("module", "disa", "managelabresults", "export.form")
                    .build()
                    .toUriString();
            model.addAttribute("exportUri", exportUri);

            try {
                model.addAttribute("disaPage", searchLabResults(searchForm));
                session.setAttribute("lastSearchParams", params);
            } catch (DisaModuleAPIException e) {
                log.error("", e);
                model.addAttribute("flashMessage", e.getLocalizedMessage());
                return "/module/disa/managelabresults/error";
            }
        }

        return "/module/disa/managelabresults/index";
    }

    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<LabResult> searchJson(@Valid SearchForm searchForm) {
        return searchLabResults(searchForm);
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(
            @Valid SearchForm searchForm,
            ModelMap model,
            HttpServletResponse response) throws Exception {

        ViralLoadResultsDelegate delegate = new ViralLoadResultsDelegate();
        delegate.createExcelFileStaging(getAllLabResults(searchForm), response, messageSourceService);
    }

    @RequestMapping(value = "/{requestId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String requestId) {
        labResultService.deleteByRequestId(requestId);
    }

    @RequestMapping(value = "/{requestId}/reschedule", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void reschedule(@PathVariable String requestId) {
        labResultService.rescheduleLabResult(requestId);
    }

    @ModelAttribute("pageTitle")
    private void setPageTitle(ModelMap model) {
        String openMrs = messageSourceService.getMessage("openmrs.title", null, Context.getLocale());
        String pageTitle = messageSourceService.getMessage("disa.list.viral.load.results.manage", null,
                Context.getLocale());
        model.addAttribute("pageTitle", openMrs + " - " + pageTitle);
    }

    /**
     * Populates SISMA code dropdown options.
     */
    private void populateSismaCodes(ModelMap model) {
        String propertyValue = administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE)
                .getPropertyValue();
        List<String> sismaCodes = Arrays.asList(propertyValue.split(","));
        List<String> sismaCodesTodos = new ArrayList<String>();

        sismaCodesTodos.addAll(sismaCodes);

        model.addAttribute("sismaCodes", sismaCodesTodos);
    }

    private Page<LabResult> searchLabResults(SearchForm searchForm) {
        LocalDate startDate = null;
        if (searchForm.getStartDate() != null) {
            startDate = searchForm.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        LocalDate endDate = null;
        if (searchForm.getEndDate() != null) {
            endDate = searchForm.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return labResultService.search(
                startDate,
                endDate,
                searchForm.getRequestId() != null ? searchForm.getRequestId().replaceAll("\\s", "") : "",
                searchForm.getVlState(),
                searchForm.getNotProcessingCause(),
                searchForm.getNid() != null ? searchForm.getNid().replaceAll("\\s", "") : "",
                labResultService.getHealthFacilityLabCodes(searchForm.getVlSisma()),
                searchForm.getSearch(),
                searchForm.getPageNumber(),
                searchForm.getPageSize(),
                searchForm.getOrderBy(),
                searchForm.getDir());
    }

    private List<LabResult> getAllLabResults(SearchForm searchForm) {
        LocalDate startDate = null;
        if (searchForm.getStartDate() != null) {
            startDate = searchForm.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        LocalDate endDate = null;
        if (searchForm.getEndDate() != null) {
            endDate = searchForm.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return labResultService.getAll(
                startDate,
                endDate,
                searchForm.getRequestId(),
                searchForm.getVlState(),
                searchForm.getNotProcessingCause(),
                searchForm.getNid(),
                labResultService.getHealthFacilityLabCodes(searchForm.getVlSisma()),
                searchForm.getSearch(),
                searchForm.getOrderBy(),
                searchForm.getDir());
    }
}

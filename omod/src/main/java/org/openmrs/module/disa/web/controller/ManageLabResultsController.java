package org.openmrs.module.disa.web.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.report.StagingServerReport;
import org.openmrs.module.disa.api.sync.SyncStatusService;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.module.disa.web.model.SearchForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/module/disa/managelabresults")
@SessionAttributes({ "flashMessage" })
public class ManageLabResultsController {

    private static final Logger log = LoggerFactory.getLogger(ManageLabResultsController.class);

    private LabResultService labResultService;

    private MessageSourceService messageSourceService;

    private AdministrationService administrationService;

    private ObjectMapper objectMapper;

    private OrgUnitService orgUnitService;

    private SyncStatusService syncStatusService;

    @Autowired
    public ManageLabResultsController(
            LabResultService labResultService,
            MessageSourceService messageSourceService,
            @Qualifier("adminService") AdministrationService administrationService,
            ObjectMapper objectMapper,
            OrgUnitService orgUnitService,
            SyncStatusService syncStatusService) {
        this.labResultService = labResultService;
        this.messageSourceService = messageSourceService;
        this.administrationService = administrationService;
        this.objectMapper = objectMapper;
        this.orgUnitService = orgUnitService;
        this.syncStatusService = syncStatusService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String search(
            @RequestParam MultiValueMap<String, String> params,
            @Valid SearchForm searchForm,
            BindingResult result,
            ModelMap model,
            HttpSession session,
            HttpServletRequest request) {

        try {

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

                model.addAttribute("disaPage", searchLabResults(searchForm));
                session.setAttribute("lastSearchParams", params);
            }

        } catch (DisaModuleAPIException e) {
            log.error("", e);
            model.addAttribute("flashMessage", e.getLocalizedMessage());
            return "/module/disa/managelabresults/error";
        }

        return "/module/disa/managelabresults/index";
    }

    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String searchJson(@Valid SearchForm searchForm) throws JsonProcessingException {
        return objectMapper.writeValueAsString(searchLabResults(searchForm));
    }

    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public String export(@Valid SearchForm searchForm, ModelMap model, HttpSession session) {

        @SuppressWarnings("unchecked")
        String query = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .queryParams((MultiValueMap<String, String>) session.getAttribute("lastSearchParams"))
                .build()
                .getQuery();

        if (searchForm.getStartDate() == null || searchForm.getEndDate() == null) {
            model.addAttribute("flashMessage", messageSourceService.getMessage("disa.error.date.range", null,
                    Context.getLocale()));
            return "redirect:/module/disa/managelabresults.form?" + query;
        }

        return "redirect:/module/disa/managelabresults/download.form?" + query;
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> download(
            @Valid SearchForm searchForm,
            ModelMap model) throws IOException {

        if (searchForm.getStartDate() == null || searchForm.getEndDate() == null) {
            model.addAttribute("flashMessage", messageSourceService.getMessage("disa.error.date.range", null,
                    Context.getLocale()));
            return ResponseEntity.badRequest().body(new byte[] {});
        }

        StagingServerReport report = new StagingServerReport(messageSourceService);
        report.addStagingServerSheet(getAllLabResults(searchForm));

        return ResponseEntity.ok()
                .contentType(new MediaType("application", "ms-excel"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + StagingServerReport.REPORT_NAME + ".xls")
                .body(report.generateReport());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        labResultService.deleteById(id);
    }

    @RequestMapping(value = "/{id}/reschedule", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void reschedule(@PathVariable long id) {
        labResultService.rescheduleLabResult(id);

    }

    @ModelAttribute("syncStatus")
    private Map<String, String> getSyncStatus() {
        HashMap<String, String> syncStatus = new HashMap<>();
        syncStatus.put("lastExecution", syncStatusService.getLastExecutionMessage());
        syncStatus.put("currentExecution", syncStatusService.getCurrentExecutionMessage());
        return syncStatus;
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
        Map<String, String> orgUnits = new HashMap<>();

        for (String code : sismaCodes) {
            OrgUnit ou = orgUnitService.getOrgUnitByCode(code);
            if (ou != null) {
                orgUnits.put(ou.getCode(), ou.getFacility() + " - " + ou.getCode());
            }
        }

        model.addAttribute("orgUnits", orgUnits);
    }

    private Page<LabResult> searchLabResults(SearchForm searchForm) {
        return labResultService.search(
                searchForm.getStartLocalDate(),
                searchForm.getEndLocalDate(),
                searchForm.getNormalizedRequestId(),
                searchForm.getLabResultStatus(),
                searchForm.getNotProcessingCauseEnum(),
                searchForm.getTypeOfResultEnum(),
                searchForm.getNid(),
                searchForm.getSismaCode().equals(Constants.ALL)
                        ? labResultService.getHealthFacilityLabCodes()
                        : Arrays.asList(searchForm.getSismaCode()),
                searchForm.getSearch(),
                searchForm.getPageNumber(),
                searchForm.getPageSize(),
                searchForm.getOrderBy(),
                searchForm.getDir());
    }

    private List<LabResult> getAllLabResults(SearchForm searchForm) {
        return labResultService.getAll(
                searchForm.getStartLocalDate(),
                searchForm.getEndLocalDate(),
                searchForm.getNormalizedRequestId(),
                searchForm.getLabResultStatus(),
                searchForm.getNotProcessingCauseEnum(),
                searchForm.getNid(),
                searchForm.getSismaCode().equals(Constants.ALL)
                        ? labResultService.getHealthFacilityLabCodes()
                        : Arrays.asList(searchForm.getSismaCode()));
    }
}

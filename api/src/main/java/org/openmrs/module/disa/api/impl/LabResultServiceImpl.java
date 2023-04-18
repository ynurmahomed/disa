package org.openmrs.module.disa.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LabResultServiceImpl implements LabResultService {

    private DisaAPIHttpClient client;
    private OrgUnitService orgUnitService;
    private AdministrationService administrationService;

    @Autowired
    public LabResultServiceImpl(
            DisaAPIHttpClient client,
            OrgUnitService orgUnitService,
            @Qualifier("adminService") AdministrationService administrationService) {
        this.client = client;
        this.orgUnitService = orgUnitService;
        this.administrationService = administrationService;
    }

    @Override
    public Page<Disa> search(
            LocalDate startDate, LocalDate endDate,
            String requestId,
            String viralLoadStatus, String notProcessingCause,
            String nid, List<String> healthFacilityLabCodes,
            String search,
            int pageNumber, int pageSize,
            String orderBy, String direction) {

        try {

            if (healthFacilityLabCodes.isEmpty()) {
                throw new DisaModuleAPIException("disa.health.facility.required", (Object[]) null);
            }

            if (Constants.ALL.equals(viralLoadStatus)) {
                viralLoadStatus = "";
            }

            if (Constants.ALL.equals(notProcessingCause)) {
                notProcessingCause = "";
            }

            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null) {
                start = startDate.atStartOfDay();
            }
            if (endDate != null) {
                end = endDate.atTime(23, 0);
            }

            return client.searchLabResults(start, end, requestId,
                    viralLoadStatus,
                    notProcessingCause, nid, healthFacilityLabCodes,
                    search,
                    pageNumber, pageSize, orderBy, direction);

        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e, healthFacilityLabCodes, "disa.result.search.error");
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.search.error", (Object[]) null, e);
        }
    }

    @Override
    public List<Disa> getAll(
            LocalDate startDate, LocalDate endDate,
            String requestId,
            String viralLoadStatus, String notProcessingCause,
            String nid, List<String> healthFacilityLabCodes,
            String search,
            String orderBy, String direction) {

        try {

            if (healthFacilityLabCodes.isEmpty()) {
                throw new DisaModuleAPIException("disa.health.facility.required", (Object[]) null);
            }

            if (Constants.ALL.equals(viralLoadStatus)) {
                viralLoadStatus = "";
            }

            if (Constants.ALL.equals(notProcessingCause)) {
                notProcessingCause = "";
            }

            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null) {
                start = startDate.atStartOfDay();
            }
            if (endDate != null) {
                end = endDate.atTime(23, 0);
            }

            return client.getAllLabResults(start, end, requestId,
                    viralLoadStatus,
                    notProcessingCause, nid, healthFacilityLabCodes,
                    search,
                    orderBy, direction);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e, healthFacilityLabCodes, "disa.result.export.error");
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.export.error", (Object[]) null, e);
        }
    }

    @Override
    public Disa getByRequestId(String requestId) {
        try {
            return client.getResultByRequestId(requestId);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e, Collections.emptyList(), "disa.result.get.error");
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.get.error", (Object[]) null, e);
        }
    }

    @Override
    public void deleteByRequestId(String requestId) {
        try {
            client.deleteResultByRequestId(requestId);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e, Collections.emptyList(), "disa.result.delete.error");
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.delete.error", (Object[]) null, e);
        }

    }

    @Override
    public Disa reallocateLabResult(Disa labResult, OrgUnit destination) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitByCode(destination.getCode());
        labResult.setHealthFacilityLabCode(orgUnit.getCode());
        labResult.setRequestingFacilityName(orgUnit.getFacility());
        labResult.setRequestingDistrictName(orgUnit.getDistrict());
        labResult.setRequestingProvinceName(orgUnit.getProvince());
        labResult.setViralLoadStatus("PENDING");
        updateLabResult(labResult);
        return labResult;
    }

    @Override
    public void rescheduleLabResult(Disa labResult) {
        labResult.setViralLoadStatus("PENDING");
        updateLabResult(labResult);
    }

    @Override
    public List<String> getHealthFacilityLabCodes(String code) {
        List<String> hfCodes = new ArrayList<>();

        if (Constants.ALL.equals(code)) {
            GlobalProperty gp = administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE);
            if (gp == null || StringUtils.isEmpty(gp.getPropertyValue())) {
                throw new DisaModuleAPIException("disa.config.sisma.codes.error",
                        new Object[] { Constants.DISA_SISMA_CODE });
            }
            hfCodes.addAll(Arrays.asList(gp.getPropertyValue().split(",")));
        } else {
            hfCodes.add(code);
        }

        return hfCodes;
    }

    private DisaModuleAPIException handleHttpResponseException(HttpResponseException e,
            List<String> healthFacilityLabCodes,
            String defaultMessage) {

        if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
            String sismaCode = null;

            // If the search contains only one health facility, then we can use that one in
            // the error message.
            // Otherwise, we need to find the first one that is not authorized.
            if (healthFacilityLabCodes.size() == 1) {
                sismaCode = healthFacilityLabCodes.get(0);
            } else {
                sismaCode = findUnauthorisedSismaCode(healthFacilityLabCodes);
            }

            // Display a generic message only if we cannot find the unauthorized health
            // facility.
            String message = "disa.result.unauthorized.generic";
            if (sismaCode != null) {
                message = "disa.result.unauthorized";
            }
            return new DisaModuleAPIException(message, new String[] { sismaCode }, e);
        }

        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            return new DisaModuleAPIException("disa.api.authentication.error", new String[] {}, e);
        }

        if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            return new DisaModuleAPIException("disa.result.not.found", (Object[]) null, e);
        }

        return new DisaModuleAPIException(defaultMessage, (Object[]) null, e);
    }

    private String findUnauthorisedSismaCode(List<String> healthFacilityLabCodes) {
        return client.findUnauthorisedSismaCode(healthFacilityLabCodes);
    }

    private void updateLabResult(Disa labResult) {
        try {
            client.updateResult(labResult);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e, Collections.singletonList(labResult.getHealthFacilityLabCode()),
                    "disa.result.update.error");
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.update.error", (Object[]) null, e);
        }
    }
}

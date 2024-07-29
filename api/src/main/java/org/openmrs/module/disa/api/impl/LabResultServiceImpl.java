package org.openmrs.module.disa.api.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.disa.api.DisaService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.OrgUnit;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.TypeOfResult;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@Service
public class LabResultServiceImpl extends BaseOpenmrsService implements LabResultService {

    private DisaAPIHttpClient client;
    private OrgUnitService orgUnitService;
    private AdministrationService administrationService;
    private DisaService disaService;

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
    public Page<LabResult> search(
            LocalDate startDate, LocalDate endDate,
            String requestId,
            LabResultStatus labResultStatus, NotProcessingCause notProcessingCause,
            TypeOfResult typeOfResult,
            String nid, List<String> healthFacilityLabCodes,
            String search,
            int pageNumber, int pageSize,
            String orderBy, String direction) {

        try {

            if (healthFacilityLabCodes.isEmpty()) {
                throw new DisaModuleAPIException("disa.health.facility.required", (Object[]) null);
            }

            LocalDateTime start = null;
            LocalDateTime end = null;
            if (startDate != null) {
                start = startDate.atStartOfDay();
            }
            if (endDate != null) {
                end = endDate.atTime(23, 0);
            }

            Page<LabResult> page = client.searchLabResults(start, end, requestId,
                    labResultStatus,
                    notProcessingCause,
                    typeOfResult,
                    nid, healthFacilityLabCodes,
                    search,
                    pageNumber, pageSize, orderBy, direction);

            disaService.loadEncounters(page.getResultList());

            return page;

        } catch (HttpStatusCodeException e) {
            throw handleHttpResponseException(e.getStatusCode().value(), healthFacilityLabCodes,
                    "disa.result.search.error");
        } catch (ResourceAccessException e) {
            if (probableConnectivityIssue(e)) {
                throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.search.error", (Object[]) null, e);
        }
    }

    @Override
    public List<LabResult> getAll(
            LocalDate startDate, LocalDate endDate,
            String requestId,
            LabResultStatus labResultStatus, NotProcessingCause notProcessingCause,
            String nid, List<String> healthFacilityLabCodes) {

        try {

            if (healthFacilityLabCodes.isEmpty()) {
                throw new DisaModuleAPIException("disa.health.facility.required", (Object[]) null);
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
                    labResultStatus,
                    notProcessingCause, nid, healthFacilityLabCodes);
        } catch (HttpStatusCodeException e) {
            throw handleHttpResponseException(e.getStatusCode().value(), healthFacilityLabCodes,
                    "disa.result.export.error");
        } catch (ResourceAccessException e) {
            if (probableConnectivityIssue(e)) {
                throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.export.error", (Object[]) null, e);
        }
    }

    @Override
    public LabResult getById(long id) {
        try {
            return client.getResultById(id);
        } catch (HttpStatusCodeException e) {
            throw handleHttpResponseException(e.getStatusCode().value(), Collections.emptyList(),
                    "disa.result.get.error");
        } catch (ResourceAccessException e) {
            if (probableConnectivityIssue(e)) {
                throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.get.error", (Object[]) null, e);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            client.deleteResultById(id);
        } catch (HttpStatusCodeException e) {
            throw handleHttpResponseException(e.getStatusCode().value(), Collections.emptyList(),
                    "disa.result.delete.error");
        } catch (ResourceAccessException e) {
            if (probableConnectivityIssue(e)) {
                throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
            } else {
                throw e;
            }
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.delete.error", (Object[]) null, e);
        }

    }

    @Override
    public LabResult reallocateLabResult(long id, OrgUnit destination) {
        OrgUnit orgUnit = orgUnitService.getOrgUnitByCode(destination.getCode());
        LabResult labResult = getById(id);
        labResult.setHealthFacilityLabCode(orgUnit.getCode());
        labResult.setRequestingFacilityName(orgUnit.getFacility());
        labResult.setRequestingDistrictName(orgUnit.getDistrict());
        labResult.setRequestingProvinceName(orgUnit.getProvince());
        labResult.setLabResultStatus(LabResultStatus.PENDING);
        updateLabResult(labResult);
        return labResult;
    }

    @Override
    public void rescheduleLabResult(long id) {
        LabResult labResult = getById(id);
        labResult.setLabResultStatus(LabResultStatus.PENDING);
        updateLabResult(labResult);
    }

    @Override
    public List<String> getHealthFacilityLabCodes() {
        List<String> hfCodes = new ArrayList<>();

        GlobalProperty gp = administrationService.getGlobalPropertyObject(Constants.DISA_SISMA_CODE);
        if (gp == null || StringUtils.isEmpty(gp.getPropertyValue())) {
            throw new DisaModuleAPIException("disa.config.sisma.codes.error",
                    new Object[] { Constants.DISA_SISMA_CODE });
        }
        hfCodes.addAll(Arrays.asList(gp.getPropertyValue().split(",")));

        return hfCodes;
    }

    @Override
    public void updateLabResult(LabResult labResult) {
        try {
            client.updateResult(labResult);
        } catch (HttpResponseException e) {
            throw handleHttpResponseException(e.getStatusCode(),
                    Collections.singletonList(labResult.getHealthFacilityLabCode()),
                    "disa.result.update.error");
        } catch (UnknownHostException | ConnectException | SocketTimeoutException e) {
            throw new DisaModuleAPIException("disa.result.no.internet", (Object[]) null, e);
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.update.error", (Object[]) null, e);
        }
    }

    @Override
    public List<LabResult> getResultsToSync() {
        return getAll(null, null, null, LabResultStatus.PENDING, null, null, getHealthFacilityLabCodes());
    }

    private boolean probableConnectivityIssue(ResourceAccessException e) {
        return e.getCause() instanceof ConnectException
                || e.getCause() instanceof SocketTimeoutException
                || e.getCause() instanceof UnknownHostException;
    }

    private DisaModuleAPIException handleHttpResponseException(int statusCode,
            List<String> healthFacilityLabCodes,
            String defaultMessage) {

        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        if (httpStatus == HttpStatus.FORBIDDEN) {
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
            return new DisaModuleAPIException(message, new String[] { sismaCode });
        }

        if (httpStatus == HttpStatus.UNAUTHORIZED) {
            return new DisaModuleAPIException("disa.api.authentication.error", new String[] {});
        }

        if (httpStatus == HttpStatus.NOT_FOUND) {
            return new DisaModuleAPIException("disa.result.not.found", (Object[]) null);
        }

        return new DisaModuleAPIException(defaultMessage, (Object[]) null);
    }

    private String findUnauthorisedSismaCode(List<String> healthFacilityLabCodes) {
        return client.findUnauthorisedSismaCode(healthFacilityLabCodes);
    }

    @Autowired
    public void setDisaService(DisaService disaService) {
        this.disaService = disaService;
    }

}

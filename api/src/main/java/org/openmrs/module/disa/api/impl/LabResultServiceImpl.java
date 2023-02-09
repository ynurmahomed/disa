package org.openmrs.module.disa.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.DisaModuleAPIException;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.Page;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
            String requestId, String referringRequestID,
            String viralLoadStatus, String notProcessingCause,
            String nid, List<String> healthFacilityLabCodes,
            int pageNumber) {

        try {

            if (healthFacilityLabCodes.isEmpty()) {
                throw new DisaModuleAPIException("disa.health.facility.required", (Object[]) null);
            }

            if (viralLoadStatus.equals(Constants.ALL)) {
                viralLoadStatus = "";
            }

            if (notProcessingCause.equals(Constants.ALL)) {
                notProcessingCause = "";
            }

            return client.searchLabResults(startDate.atStartOfDay(), endDate.atTime(23, 0), requestId,
                    referringRequestID, viralLoadStatus,
                    notProcessingCause, nid, healthFacilityLabCodes,
                    pageNumber);

        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.search.error", (Object[]) null, e);
        }
    }

    @Override
    public Disa getByRequestId(String requestId) {
        try {
            return client.getResultByRequestId(requestId);
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.get.error", (Object[]) null, e);
        }
    }

    @Override
    public void deleteByRequestId(String requestId) {
        try {
            client.deleteResultByRequestId(requestId);
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

    private void updateLabResult(Disa labResult) {
        try {
            client.updateResult(labResult);
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.update.error", (Object[]) null, e);
        }
    }

    @Override
    public List<String> getHealthFacilityLabCodes(String code) {
        List<String> hfCodes = new ArrayList<>();

        if (Constants.TODOS.equals(code)) {
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
}

package org.openmrs.module.disa.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.DisaModuleAPIException;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.OrgUnitService;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabResultServiceImpl implements LabResultService {

    private DisaAPIHttpClient client;
    private OrgUnitService orgUnitService;

    @Autowired
    public LabResultServiceImpl(DisaAPIHttpClient client, OrgUnitService orgUnitService) {
        this.client = client;
        this.orgUnitService = orgUnitService;
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
}

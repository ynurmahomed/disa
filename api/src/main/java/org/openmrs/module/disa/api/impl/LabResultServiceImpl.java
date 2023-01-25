package org.openmrs.module.disa.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.api.DisaModuleAPIException;
import org.openmrs.module.disa.api.LabResultService;
import org.openmrs.module.disa.api.client.DisaAPIHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabResultServiceImpl implements LabResultService {

    private DisaAPIHttpClient client;

    @Autowired
    public LabResultServiceImpl(DisaAPIHttpClient client) {
        this.client = client;
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
    public void updateLabResult(Disa labResult) {
        try {
            client.updateResult(labResult);
        } catch (IOException | URISyntaxException e) {
            throw new DisaModuleAPIException("disa.result.update.error", (Object[]) null, e);
        }
    }

}

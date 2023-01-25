package org.openmrs.module.disa.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.disa.Disa;

public interface LabResultService {
    @Authorized({ "Gerir resultados no Disa Interoperabilidade" })
    Disa getByRequestId(String requestId);

    @Authorized({ "Gerir resultados no Disa Interoperabilidade" })
    void deleteByRequestId(String requestId);

    @Authorized({ "Gerir resultados no Disa Interoperabilidade" })
    void updateLabResult(Disa labResult);
}

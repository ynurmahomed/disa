package org.openmrs.module.disa.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;

public interface LabResultService {
    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    Disa getByRequestId(String requestId);

    @Authorized({ "Remover resultados no Disa Interoperabilidade" })
    void deleteByRequestId(String requestId);

    @Authorized({ "Realocar resultados no Disa Interoperabilidade" })
    Disa reallocateLabResult(Disa labResult, OrgUnit destination);

    @Authorized({ "Reagendar resultados no Disa Interoperabilidade" })
    void rescheduleLabResult(Disa labResult);
}

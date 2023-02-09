package org.openmrs.module.disa.api;

import java.time.LocalDate;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.disa.Disa;
import org.openmrs.module.disa.OrgUnit;

public interface LabResultService {
    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    Page<Disa> search(
            LocalDate startDate,
            LocalDate endDate,
            String requestId,
            String referringRequestID,
            String viralLoadStatus,
            String notProcessingCause,
            String nid,
            List<String> healthFacilityLabCodes,
            int pageNumber);

    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    Disa getByRequestId(String requestId);

    @Authorized({ "Remover resultados no Disa Interoperabilidade" })
    void deleteByRequestId(String requestId);

    @Authorized({ "Realocar resultados no Disa Interoperabilidade" })
    Disa reallocateLabResult(Disa labResult, OrgUnit destination);

    @Authorized({ "Reagendar resultados no Disa Interoperabilidade" })
    void rescheduleLabResult(Disa labResult);

    List<String> getHealthFacilityLabCodes(String code);
}

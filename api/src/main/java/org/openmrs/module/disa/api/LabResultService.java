package org.openmrs.module.disa.api;

import java.time.LocalDate;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.TypeOfResult;

public interface LabResultService {
    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    Page<LabResult> search(
            LocalDate startDate,
            LocalDate endDate,
            String requestId,
            String labResultStatus,
            String notProcessingCause,
            TypeOfResult typeOfResult,
            String nid,
            List<String> healthFacilityLabCodes,
            String search,
            int pageNumber,
            int pageSize,
            String orderBy,
            String direction);

    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    List<LabResult> getAll(
            LocalDate startDate,
            LocalDate endDate,
            String requestId,
            String labResultStatus,
            String notProcessingCause,
            String nid,
            List<String> healthFacilityLabCodes,
            String search,
            String orderBy,
            String direction);

    @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
    LabResult getByRequestId(String requestId);

    @Authorized({ "Remover resultados no Disa Interoperabilidade" })
    void deleteByRequestId(String requestId);

    @Authorized({ "Realocar resultados no Disa Interoperabilidade" })
    LabResult reallocateLabResult(String requestId, OrgUnit destination);

    @Authorized({ "Reagendar resultados no Disa Interoperabilidade" })
    void rescheduleLabResult(String requestId);

    List<String> getHealthFacilityLabCodes(String code);
}

package org.openmrs.module.disa.api;

import java.time.LocalDate;
import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.TypeOfResult;

public interface LabResultService extends OpenmrsService {
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
                        List<String> healthFacilityLabCodes);

        @Authorized({ "Pesquisar resultados no Disa Interoperabilidade" })
        LabResult getById(long id);

        @Authorized({ "Remover resultados no Disa Interoperabilidade" })
        void deleteById(long id);

        @Authorized({ "Realocar resultados no Disa Interoperabilidade" })
        LabResult reallocateLabResult(long id, OrgUnit destination);

        @Authorized({ "Reagendar resultados no Disa Interoperabilidade" })
        void rescheduleLabResult(long id);

        // TODO might need to give this privilege to the lab technician and admin
        @Authorized({ "Atualizar resultados no Disa Interoperabilidade" })
        void updateLabResult(LabResult labResult);

        List<LabResult> getResultsToSync();

        /**
         * @return List of configured sisma codes.
         */
        List<String> getHealthFacilityLabCodes();
}

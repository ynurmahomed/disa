package org.openmrs.module.disa.api;

import java.util.List;

public interface OrgUnitService {
    OrgUnit getOrgUnitByCode(String code);

    List<OrgUnit> searchOrgUnits(String q);
}

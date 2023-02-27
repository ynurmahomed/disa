package org.openmrs.module.disa.api;

import java.util.List;

import org.openmrs.module.disa.OrgUnit;

public interface OrgUnitService {
    OrgUnit getOrgUnitByCode(String code);

    List<OrgUnit> searchOrgUnits(String q);
}

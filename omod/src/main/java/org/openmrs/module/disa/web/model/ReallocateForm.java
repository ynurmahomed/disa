package org.openmrs.module.disa.web.model;

import org.hibernate.validator.constraints.NotBlank;

public class ReallocateForm {

    @NotBlank
    private String healthFacilityLabCode;

    public String getHealthFacilityLabCode() {
        return healthFacilityLabCode;
    }

    public void setHealthFacilityLabCode(String healthFacilityLabCode) {
        this.healthFacilityLabCode = healthFacilityLabCode;
    }
}

package org.openmrs.module.disa.api;

public class OrgUnit {
    private String code;
    private String province;
    private String district;
    private String facility;

    public OrgUnit() {
    }

    public OrgUnit(String code) {
        this.code = code;
    }

    public OrgUnit(String code, String province, String district, String facility) {
        this.code = code;
        this.province = province;
        this.district = district;
        this.facility = facility;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }
}

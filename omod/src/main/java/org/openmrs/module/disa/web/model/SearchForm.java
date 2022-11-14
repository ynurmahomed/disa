package org.openmrs.module.disa.web.model;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SearchForm {
    
    @Size(max = 22)
    private String requestId;
    
    @Size(max = 21)
    private String nid;
    
    private String vlSisma;
    
    @Size(max = 16)
    private String referringId;
    
    private String vlState;

    @NotNull()
    private Date startDate;

    @NotNull()
    private Date endDate;

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getNid() {
        return this.nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getVlSisma() {
        return this.vlSisma;
    }

    public void setVlSisma(String vlSisma) {
        this.vlSisma = vlSisma;
    }

    public String getReferringId() {
        return this.referringId;
    }

    public void setReferringId(String referringId) {
        this.referringId = referringId;
    }

    public String getVlState() {
        return this.vlState;
    }

    public void setVlState(String vlState) {
        this.vlState = vlState;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}


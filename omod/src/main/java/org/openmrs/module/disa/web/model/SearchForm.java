package org.openmrs.module.disa.web.model;

import java.time.LocalDate;

import javax.validation.constraints.Size;

import org.openmrs.module.disa.api.util.Constants;

public class SearchForm {

    @Size(max = 22)
    private String requestId;

    @Size(max = 21)
    private String nid;

    private String vlSisma;

    @Size(max = 16)
    private String referringId;

    private String vlState;

    private String notProcessingCause;

    private LocalDate startDate;

    private LocalDate endDate;

    private int pageNumber;

    private int pageSize;

    private String orderBy;

    private String dir;

    public SearchForm() {
        this.vlState = Constants.ALL;
        this.notProcessingCause = Constants.ALL;
        this.vlSisma = Constants.ALL;
    }

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

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getNotProcessingCause() {
        return notProcessingCause;
    }

    public void setNotProcessingCause(String notProcessingCause) {
        this.notProcessingCause = notProcessingCause;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return "SearchForm [requestId=" + requestId + ", nid=" + nid + ", vlSisma=" + vlSisma + ", referringId="
                + referringId + ", vlState=" + vlState + ", notProcessingCause=" + notProcessingCause + ", startDate="
                + startDate + ", endDate=" + endDate + ", pageNumber=" + pageNumber + ", pageSize=" + pageSize
                + ", orderBy=" + orderBy + ", dir=" + dir + "]";
    }
}

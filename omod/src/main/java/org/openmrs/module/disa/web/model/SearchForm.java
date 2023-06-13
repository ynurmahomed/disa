package org.openmrs.module.disa.web.model;

import java.util.Date;

import javax.validation.constraints.Size;

import org.openmrs.module.disa.TypeOfResult;
import org.openmrs.module.disa.api.util.Constants;

public class SearchForm {

    public static final String REQUEST_ID_PREFIX = "MZDISA";

    @Size(max = 22)
    private String requestId;

    @Size(max = 21)
    private String nid;

    private String vlSisma;

    private String vlState;

    private String notProcessingCause;

    private Date startDate;

    private Date endDate;

    private int pageNumber;

    private int pageSize;

    private String orderBy;

    private String dir;

    private String search;

    private TypeOfResult typeOfResult;

    public SearchForm() {
        this.vlState = Constants.ALL;
        this.notProcessingCause = Constants.ALL;
        this.vlSisma = Constants.ALL;
        this.requestId = REQUEST_ID_PREFIX;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        String reqId = clearWhiteSpace(requestId);
        if (!reqId.startsWith(REQUEST_ID_PREFIX)) {
            this.requestId = REQUEST_ID_PREFIX + reqId;
        } else {
            this.requestId = reqId;
        }
    }

    public String getNid() {
        return this.nid;
    }

    public void setNid(String nid) {
        this.nid = clearWhiteSpace(nid);
    }

    public String getVlSisma() {
        return this.vlSisma;
    }

    public void setVlSisma(String vlSisma) {
        this.vlSisma = vlSisma;
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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public TypeOfResult getTypeOfResult() {
        return typeOfResult;
    }

    public void setTypeOfResult(TypeOfResult typeOfResult) {
        this.typeOfResult = typeOfResult;
    }

    private String clearWhiteSpace(String str) {
        return str != null ? str.replaceAll("\\s", "") : "";
    }

    @Override
    public String toString() {
        return "SearchForm [requestId=" + requestId + ", nid=" + nid + ", vlSisma=" + vlSisma
                + ", vlState=" + vlState + ", notProcessingCause=" + notProcessingCause + ", startDate="
                + startDate + ", endDate=" + endDate + ", pageNumber=" + pageNumber + ", pageSize=" + pageSize
                + ", orderBy=" + orderBy + ", dir=" + dir + ", search=" + search + "]";
    }
}

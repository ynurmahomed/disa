package org.openmrs.module.disa.web.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.validation.constraints.Size;

import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.NotProcessingCause;
import org.openmrs.module.disa.api.TypeOfResult;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.util.StringUtils;

public class SearchForm {

    public static final String REQUEST_ID_PREFIX = "MZDISA";

    @Size(max = 22)
    private String requestId;

    @Size(max = 21)
    private String nid;

    private String sismaCode;

    private String vlState;

    private String notProcessingCause;

    private Date startDate;

    private Date endDate;

    private int pageNumber;

    private int pageSize;

    private String orderBy;

    private String dir;

    private String search;

    private String typeOfResult;

    public SearchForm() {
        this.vlState = Constants.ALL;
        this.notProcessingCause = Constants.ALL;
        this.sismaCode = Constants.ALL;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public String getNormalizedRequestId() {
        if (!StringUtils.isEmpty(requestId) && !requestId.startsWith(SearchForm.REQUEST_ID_PREFIX)) {
            return SearchForm.REQUEST_ID_PREFIX + requestId;
        } else {
            return requestId;
        }
    }

    public void setRequestId(String requestId) {
        this.requestId = clearWhiteSpace(requestId);

    }

    public String getNid() {
        return this.nid;
    }

    public void setNid(String nid) {
        this.nid = clearWhiteSpace(nid);
    }

    public String getSismaCode() {
        return this.sismaCode;
    }

    public void setSismaCode(String vlSisma) {
        this.sismaCode = vlSisma;
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

    public LocalDate getStartLocalDate() {
        if (startDate != null) {
            return startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public LocalDate getEndLocalDate() {
        if (endDate != null) {
            return endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNotProcessingCause() {
        return notProcessingCause;
    }

    public NotProcessingCause getNotProcessingCauseEnum() {
        return notProcessingCause == null || Constants.ALL.equals(notProcessingCause) ? null
                : NotProcessingCause.valueOf(notProcessingCause);
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

    public String getTypeOfResult() {
        return typeOfResult;
    }

    public void setTypeOfResult(String typeOfResult) {
        this.typeOfResult = typeOfResult;
    }

    public TypeOfResult getTypeOfResultEnum() {
        return typeOfResult == null || Constants.ALL.equals(typeOfResult) ? null : TypeOfResult.valueOf(typeOfResult);
    }

    public LabResultStatus getLabResultStatus() {
        return vlState == null || Constants.ALL.equals(vlState) ? null : LabResultStatus.valueOf(vlState);
    }

    private String clearWhiteSpace(String str) {
        return str != null ? str.replaceAll("\\s", "") : "";
    }

    @Override
    public String toString() {
        return "SearchForm [requestId=" + requestId + ", nid=" + nid + ", sismaCode=" + sismaCode
                + ", vlState=" + vlState + ", notProcessingCause=" + notProcessingCause + ", startDate="
                + startDate + ", endDate=" + endDate + ", pageNumber=" + pageNumber + ", pageSize=" + pageSize
                + ", orderBy=" + orderBy + ", dir=" + dir + ", search=" + search + "]";
    }
}

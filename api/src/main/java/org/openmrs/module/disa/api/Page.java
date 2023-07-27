package org.openmrs.module.disa.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Page<T> {

    private int pageNumber;

    private int pageSize;

    private long totalResults;

    private List<T> resultList;

    public Page() {
    }

    public Page(int pageNumber, int pageSize, long totalResults, List<T> resultList) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalResults = totalResults;
        this.resultList = resultList;
    }

    @JsonProperty("pageNumber")
    public int getPageNumber() {
        return pageNumber;
    }

    @JsonProperty("number")
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @JsonProperty("pageSize")
    public int getPageSize() {
        return pageSize;
    }

    @JsonProperty("size")
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @JsonProperty("totalResults")
    public long getTotalResults() {
        return totalResults;
    }

    @JsonProperty("totalElements")
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    @JsonProperty("resultList")
    public List<T> getResultList() {
        return resultList;
    }

    @JsonProperty("content")
    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }
}

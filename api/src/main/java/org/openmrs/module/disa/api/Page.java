package org.openmrs.module.disa.api;

import java.util.List;

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

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }
}

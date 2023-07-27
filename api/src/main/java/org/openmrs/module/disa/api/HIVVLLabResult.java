package org.openmrs.module.disa.api;

public class HIVVLLabResult extends LabResult {

    private String viralLoadResultCopies;

    private String viralLoadResultLog;

    private String hivViralLoadResult;

    private String lastViralLoadResult;

    private String lastViralLoadDate;

    public HIVVLLabResult() {
        setTypeOfResult(TypeOfResult.HIVVL);
    }

    public HIVVLLabResult(long l) {
        super(l);
        setTypeOfResult(TypeOfResult.HIVVL);
    }

    public String getViralLoadResultCopies() {
        return viralLoadResultCopies;
    }

    public void setViralLoadResultCopies(String viralLoadResultCopies) {
        this.viralLoadResultCopies = viralLoadResultCopies;
    }

    public String getViralLoadResultLog() {
        return viralLoadResultLog;
    }

    public void setViralLoadResultLog(String viralLoadResultLog) {
        this.viralLoadResultLog = viralLoadResultLog;
    }

    public String getHivViralLoadResult() {
        return hivViralLoadResult;
    }

    public void setHivViralLoadResult(String hivViralLoadResult) {
        this.hivViralLoadResult = hivViralLoadResult;
    }

    public String getLastViralLoadResult() {
        return lastViralLoadResult;
    }

    public void setLastViralLoadResult(String lastViralLoadResult) {
        this.lastViralLoadResult = lastViralLoadResult;
    }

    public String getLastViralLoadDate() {
        return lastViralLoadDate;
    }

    public void setLastViralLoadDate(String lastViralLoadDate) {
        this.lastViralLoadDate = lastViralLoadDate;
    }

    @Override
    public String toString() {
        return "HIVVLLabResult [requestId=" + getRequestId() + "]";
    }
}

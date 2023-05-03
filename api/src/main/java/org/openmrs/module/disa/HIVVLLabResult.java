package org.openmrs.module.disa;

public class HIVVLLabResult extends LabResult {

    private String viralLoadResultCopies;

	private String viralLoadResultLog;

	private String hivViralLoadResult;

    private String lastViralLoadResult;

	private String lastViralLoadDate;

    private String finalViralLoadResult;

    public HIVVLLabResult() {
        setTypeOfResult(TypeOfResult.HIVVL);
    }

    public HIVVLLabResult(String requestId) {
        super(requestId);
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

    public String getFinalViralLoadResult() {
        return finalViralLoadResult;
    }

    public void setFinalViralLoadResult(String finalViralLoadResult) {
        this.finalViralLoadResult = finalViralLoadResult;
    }

    @Override
    public String getDisplayResult() {
        return finalViralLoadResult;
    }
}

package org.openmrs.module.disa.api;

public class CD4LabResult extends LabResult {

    private String cd4Percentage;

    public CD4LabResult() {
        setTypeOfResult(TypeOfResult.CD4);
    }

    public CD4LabResult(long l) {
        super(l);
        setTypeOfResult(TypeOfResult.CD4);
    }

    public String getCd4Percentage() {
        return cd4Percentage;
    }

    public void setCd4Percentage(String cd4Percentage) {
        this.cd4Percentage = cd4Percentage;
    }

    @Override
    public String toString() {
        return "CD4LabResult [requestId=" + getRequestId() + "]";
    }
}

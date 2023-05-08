package org.openmrs.module.disa;

public class CD4LabResult extends LabResult {

    private String cd4Percentage;

    private Integer cd4FinalResult;

    public CD4LabResult() {
        setTypeOfResult(TypeOfResult.CD4);
    }

    public CD4LabResult(String requestId) {
        super(requestId);
        setTypeOfResult(TypeOfResult.CD4);
    }

    public String getCd4Percentage() {
        return cd4Percentage;
    }

    public void setCd4Percentage(String cd4Percentage) {
        this.cd4Percentage = cd4Percentage;
    }

    public Integer getCd4FinalResult() {
        return cd4FinalResult;
    }

    public void setCd4FinalResult(Integer cd4FinalResult) {
        this.cd4FinalResult = cd4FinalResult;
    }

    @Override
    public String getDisplayResult() {
        return cd4FinalResult.toString();
    }
}

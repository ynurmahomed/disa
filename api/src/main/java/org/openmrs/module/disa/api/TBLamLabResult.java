package org.openmrs.module.disa.api;

public class TBLamLabResult extends LabResult {

    private String positivityLevel;

    public TBLamLabResult() {
        setTypeOfResult(TypeOfResult.TBLAM);
    }

    public TBLamLabResult(long l) {
        super(l);
        setTypeOfResult(TypeOfResult.TBLAM);
    }

    public String getPositivityLevel() {
        return positivityLevel;
    }

    public void setPositivityLevel(String positivityLevel) {
        this.positivityLevel = positivityLevel;
    }

    @Override
    public String toString() {
        return "TBLamLabResult [requestId=" + getRequestId() + "]";
    }
}

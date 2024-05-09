package org.openmrs.module.disa.api;

public class CRAGLabResult extends LabResult {

    public CRAGLabResult() {
        setTypeOfResult(TypeOfResult.CRAG);
    }

    public CRAGLabResult(long l) {
        super(l);
        setTypeOfResult(TypeOfResult.CRAG);
    }
}

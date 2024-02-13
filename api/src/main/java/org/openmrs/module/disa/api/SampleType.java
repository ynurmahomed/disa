package org.openmrs.module.disa.api;

/**
 * Represents specimen type
 */
public enum SampleType {

    // Sangue Seco em Papel de Filtro
    DBS("7c288beb-548c-4440-8f12-4f62cd45305a"),
    // Plasma
    PL("1002AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    // Plasma Seco
    PSC("41621b9f-49d9-48f7-aea8-92a70c7db2c0");

    private String conceptUuid;

    private SampleType(String uuid) {
        this.conceptUuid = uuid;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }
}

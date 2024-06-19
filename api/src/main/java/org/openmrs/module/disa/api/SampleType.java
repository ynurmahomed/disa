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
    PSC("f17b6cf1-7bed-4d1f-bf70-e09e3a0e4357"),
    // Soro
    SER("1001AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),
    // Liquido Cefalo raquidiano
    LCR("159995AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

    private String conceptUuid;

    private SampleType(String uuid) {
        this.conceptUuid = uuid;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }
}

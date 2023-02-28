package org.openmrs.module.disa.extension.html;

import org.openmrs.module.web.extension.LinkExt;

public class GutterListExt extends LinkExt {

    String url = "/module/disa/managelabresults.form?vlState=NOT_PROCESSED";
    String label = "Disa Interoperabilidade";

    public String getLabel() {
        return this.label;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * Returns the required privilege in order to see this section. Can be a
     * comma delimited list of privileges. If the default empty string is
     * returned, only an authenticated user is required
     *
     * @return Privilege string
     */
    public String getRequiredPrivilege() {
        return "Pesquisar resultados no Disa Interoperabilidade";
    }

}
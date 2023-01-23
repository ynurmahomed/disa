package org.openmrs.module.disa.web.controller;

import java.util.List;

import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.web.delegate.DelegateException;
import org.openmrs.module.disa.web.delegate.ManageVLResultsDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/module/disa/orgunits")
public class OrgUnitsController {

    private ManageVLResultsDelegate manageVLResultsDelegate;

    @Autowired
    public OrgUnitsController(ManageVLResultsDelegate manageVLResultsDelegate) {
        this.manageVLResultsDelegate = manageVLResultsDelegate;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<OrgUnit> searchOrgUnits(@RequestParam String term, Model model) throws DelegateException {
        return this.manageVLResultsDelegate.searchOrgUnits(term);
    }
}

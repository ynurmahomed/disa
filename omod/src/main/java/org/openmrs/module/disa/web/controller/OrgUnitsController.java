package org.openmrs.module.disa.web.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.disa.OrgUnit;
import org.openmrs.module.disa.api.DisaModuleAPIException;
import org.openmrs.module.disa.api.OrgUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO Should be an OpenMRS rest web service
@RestController
@RequestMapping("/module/disa/orgunits")
public class OrgUnitsController {

    private static final Logger log = LoggerFactory.getLogger(OrgUnitsController.class);

    private OrgUnitService orgUnitService;

    @Autowired
    public OrgUnitsController(OrgUnitService orgUnitService) {
        this.orgUnitService = orgUnitService;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OrgUnit> searchOrgUnits(@RequestParam String term) {
        return orgUnitService.searchOrgUnits(term);
    }

    /*
     * Whenever an DisaModuleAPIException is thrown return an
     * HttpStatus.INTERNAL_SERVER_ERROR.
     * This facilitates error handling on the client side.
     */
    @ExceptionHandler(DisaModuleAPIException.class)
    public ResponseEntity<Map<String, String>> handleDisaModuleAPIException(DisaModuleAPIException e) {
        log.error("", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getLocalizedMessage()));
    }
}

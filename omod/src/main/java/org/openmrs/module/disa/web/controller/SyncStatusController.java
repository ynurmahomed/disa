package org.openmrs.module.disa.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.disa.api.sync.SyncStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/module/disa/syncstatus")
public class SyncStatusController {

    private SyncStatusService syncStatusService;

    @Autowired
    public SyncStatusController(SyncStatusService syncStatusService) {
        this.syncStatusService = syncStatusService;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> status() {
        HashMap<String, String> syncStatus = new HashMap<>();
        syncStatus.put("lastExecution", syncStatusService.getLastExecutionMessage());
        syncStatus.put("currentExecution", syncStatusService.getCurrentExecutionMessage());
        return syncStatus;
    }
}

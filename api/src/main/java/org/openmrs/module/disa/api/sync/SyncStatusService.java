package org.openmrs.module.disa.api.sync;

import java.time.format.DateTimeFormatter;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.disa.api.sync.scheduler.ViralLoadFormSchedulerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SyncStatusService {
    private static final String DATE_PATTERN = "d MMMM y HH:mm";

    private MessageSourceService messageSourceService;

    @Autowired
    public SyncStatusService(MessageSourceService messageSourceService) {
        this.messageSourceService = messageSourceService;
    }

    public String getLastExecutionMessage() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_PATTERN, Context.getLocale());
        SyncStatus syncStatus = ViralLoadFormSchedulerTask.getSyncStatus();
        if (syncStatus.getLastExecutionTime() != null) {
            String[] args = new String[2];
            args[0] = syncStatus.getLastExecutionTime().format(fmt);
            args[1] = formatInterval(syncStatus.getRepeatInterval());
            return messageSourceService.getMessage("disa.syncStatus.lastExecution", args,
                    Context.getLocale());
        }
        return null;
    }

    public String getCurrentExecutionMessage() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATE_PATTERN, Context.getLocale());
        SyncStatus syncStatus = ViralLoadFormSchedulerTask.getSyncStatus();
        if (syncStatus.isExecuting()) {
            String progress = String.format("%.0f%%", syncStatus.getProgress() * 100);
            String[] args = new String[] { progress, syncStatus.getStartedExecutionTime().format(fmt) };
            return messageSourceService.getMessage("disa.syncStatus.currentExecution", args, Context.getLocale());
        }
        return null;
    }

    private String formatInterval(Long interval) {
        if (interval < 60) {
            return interval + " "
                    + messageSourceService.getMessage("Scheduler.scheduleForm.repeatInterval.units.seconds");
        } else if (interval < 3600) {
            return interval / 60 + " "
                    + messageSourceService.getMessage("Scheduler.scheduleForm.repeatInterval.units.minutes");
        } else if (interval < 86400) {
            return interval / 3600 + " "
                    + messageSourceService.getMessage("Scheduler.scheduleForm.repeatInterval.units.hours");
        } else {
            return interval / 86400 + " "
                    + messageSourceService.getMessage("Scheduler.scheduleForm.repeatInterval.units.days");
        }
    }
}

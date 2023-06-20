package org.openmrs.module.disa.api.util;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Notifier {

    private AdministrationService administrationService;

    private LocationService locationService;

    @Autowired
    public Notifier(@Qualifier("adminService") AdministrationService administrationService,
            LocationService locationService) {
        this.administrationService = administrationService;
        this.locationService = locationService;
    }

    public void notify(String subject, String body, String module) {
        String endpointUrl = administrationService.getGlobalPropertyObject(Constants.DISA_API_NOTIFICATION_URL)
                .getPropertyValue();

        String to = administrationService.getGlobalPropertyObject(Constants.DISA_API_MAIL_TO).getPropertyValue();

        NotificationUtil.sendEmail(
                endpointUrl,
                to,
                subject + locationService.getDefaultLocation().getName(),
                body,
                module);
    }

}

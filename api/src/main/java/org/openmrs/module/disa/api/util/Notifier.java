package org.openmrs.module.disa.api.util;

import static org.springframework.util.StringUtils.isEmpty;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Notifier {

    private static final Logger log = LoggerFactory.getLogger(Notifier.class);

    private AdministrationService administrationService;

    private LocationService locationService;

    private NotificationUtil notificationUtil;

    @Autowired
    public Notifier(@Qualifier("adminService") AdministrationService administrationService,
            LocationService locationService, NotificationUtil notificationUtil) {
        this.administrationService = administrationService;
        this.locationService = locationService;
        this.notificationUtil = notificationUtil;
    }

    public void notify(String subject, String body, String module, String startDate, String endDate, String repoLink ,String resultFlag) {
        String endpointUrl = administrationService.getGlobalPropertyObject(Constants.DISA_API_NOTIFICATION_URL)
                .getPropertyValue();

        String to = administrationService.getGlobalPropertyObject(Constants.DISA_API_MAIL_TO).getPropertyValue();

        if (!isEmpty(to)) {
            notificationUtil.sendEmail(
                    endpointUrl,
                    to,
                    subject + locationService.getDefaultLocation().getName(),
                    body,
                    module,
                    startDate,
                    endDate,
                    repoLink,
                    resultFlag);
        } else {
            log.info("No configured recipients. Skiping notification.");
        }

    }

}

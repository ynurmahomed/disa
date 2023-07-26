package org.openmrs.module.disa.api.sync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.LabResultStatus;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class LocationLookup extends BaseLabResultHandler {

    public static final String LOCATION_KEY = "LOCATION";

    private LocationService locationService;

    private AdministrationService adminService;

    @Autowired
    public LocationLookup(
            LocationService locationService,
            @Qualifier("adminService") AdministrationService administrationService) {
        this.locationService = locationService;
        this.adminService = administrationService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {
        Location location = getLocationByHealthFacilityLabCode(labResult.getHealthFacilityLabCode());

        if (location == null) {
            throw new DisaModuleAPIException("Could not find location with SISMA code " + labResult.getHealthFacilityLabCode());
        } else {
            getSyncContext().put(LOCATION_KEY, location);
        }

        return super.handle(labResult);
    }

    private Location getLocationByHealthFacilityLabCode(String hfLabCode) {
        String uuid = adminService
                .getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID).getPropertyValue();

        LocationAttributeType healthFacilityCode = locationService
                .getLocationAttributeTypeByUuid(uuid);

        Map<LocationAttributeType, Object> attrs = new HashMap<>();
        attrs.put(healthFacilityCode, hfLabCode);

        List<Location> locations = locationService.getLocations(null, null, attrs, false, null, null);

        if (locations.isEmpty()) {
            return null;
        }

        return locations.get(0);
    }

}

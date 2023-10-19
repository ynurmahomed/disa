package org.openmrs.module.disa.api.sync;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.module.disa.api.HIVVLLabResult;
import org.openmrs.module.disa.api.LabResult;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.openmrs.module.disa.api.util.Constants;
import org.openmrs.test.BaseContextMockTest;

public class LocationLookupTest extends BaseContextMockTest {

    @Mock
    private LocationService locationService;

    @Mock
    private AdministrationService adminService;

    @InjectMocks
    private LocationLookup locationLookup;

    @Mock
    private LabResultHandler next;

    private Location location;

    @Before
    public void setUp() {
        String uuid = "132895aa-1c88-11e8-b6fd-7395830b63f3";
        GlobalProperty locationAttrTypeGp = new GlobalProperty(Constants.LOCATION_ATTRIBUTE_TYPE_UUID, uuid);
        when(adminService.getGlobalPropertyObject(Constants.LOCATION_ATTRIBUTE_TYPE_UUID))
                .thenReturn(locationAttrTypeGp);

        LocationAttributeType locationAttrType = new LocationAttributeType();
        when(locationService.getLocationAttributeTypeByUuid(uuid))
                .thenReturn(locationAttrType);

        location = new Location();
        location.setUuid("c5242910-b396-41d1-9729-a3fbc03057b1");
        when(locationService.getLocations(
                isNull(String.class),
                isNull(Location.class),
                anyMapOf(LocationAttributeType.class, Object.class),
                eq(false),
                isNull(Integer.class),
                isNull(Integer.class)))
                .thenReturn(Collections.singletonList(location));

        locationLookup.setNext(next);
    }


    @Test
    public void shouhouldAddTheLocationToSyncContext() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setHealthFacilityLabCode("1234");

        locationLookup.handle(labResult);

        assertThat(locationLookup.getSyncContext().get(LocationLookup.LOCATION_KEY), is(location));

        // Calls the next handler
        verify(next, times(1)).handle(labResult);
    }

    @Test(expected = DisaModuleAPIException.class)
    public void shouhouldThrowExceptionIfLocationNotFound() {
        LabResult labResult = new HIVVLLabResult();
        labResult.setHealthFacilityLabCode("1234");

        when(locationService.getLocations(
                isNull(String.class),
                isNull(Location.class),
                anyMapOf(LocationAttributeType.class, Object.class),
                eq(false),
                isNull(Integer.class),
                isNull(Integer.class)))
                .thenReturn(Collections.emptyList());

        locationLookup.handle(labResult);

    }
}

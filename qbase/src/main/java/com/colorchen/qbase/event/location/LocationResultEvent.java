package com.colorchen.qbase.event.location;

import com.colorchen.qbase.model.LocationResult;

/**
 * Created by wangsye on 2017-11-24.
 */

public class LocationResultEvent {
    public enum Status {success, failed}

    public Status status;
    public LocationResult locationResult;

    public LocationResultEvent(Status status, LocationResult locationResult) {
        this.status = status;
        this.locationResult = locationResult;
    }
}

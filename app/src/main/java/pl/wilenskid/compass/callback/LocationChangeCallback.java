package pl.wilenskid.compass.callback;

import android.location.Location;

public interface LocationChangeCallback {
    void apply(Location location, float declination);
}

package pl.wilenskid.compass.callback;

public interface AzimuthChangeCallback {
    void apply(float azimuth, float magneticFieldStrength);
}

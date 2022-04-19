package pl.wilenskid.compass.listener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import pl.wilenskid.compass.R;
import pl.wilenskid.compass.callback.AzimuthChangeCallback;
import pl.wilenskid.compass.callback.LocationChangeCallback;
import pl.wilenskid.compass.callback.SensorAccuracyChangeCallback;
import pl.wilenskid.compass.model.Vector;

public class CompassDataListener implements LocationListener, SensorEventListener {

    private static final float ALPHA = 0.97f;
    private static final int MAX_ANGLE = 360;
    private static final long MIN_TIME = 0L;
    private static final float MIN_DISTANCE = 0.0f;

    public AzimuthChangeCallback azimuthChangeCallback;
    public SensorAccuracyChangeCallback sensorAccuracyChangeCallback;
    public LocationChangeCallback locationChangeCallback;

    private final Context context;
    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final Sensor magneticFieldSensor;
    private final Vector gravityVector;
    private final Vector geomagneticVector;
    private final float[] rotationMatrix;
    private final float[] inclinationMatrix;

    public CompassDataListener(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.gravityVector = new Vector();
        this.geomagneticVector = new Vector();
        this.rotationMatrix = new float[9];
        this.inclinationMatrix = new float[9];

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        synchronized (this) {
            if (azimuthChangeCallback == null) {
                return;
            }

            float magneticFieldStrength = 0;

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravityVector.multiply(ALPHA).add(new Vector(sensorEvent.values).multiply(1 - ALPHA));
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagneticVector.multiply(ALPHA).add(new Vector(sensorEvent.values).multiply(1 - ALPHA));
                float powX = geomagneticVector.getX() * geomagneticVector.getX();
                float powY = geomagneticVector.getY() * geomagneticVector.getY();
                float powZ = geomagneticVector.getZ() * geomagneticVector.getZ();
                magneticFieldStrength = (float) Math.sqrt(powX + powY + powZ);
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityVector.asArray(), geomagneticVector.asArray())) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuth = (float) (Math.toDegrees(orientation[0]) + MAX_ANGLE) % MAX_ANGLE;
                azimuthChangeCallback.apply(azimuth, magneticFieldStrength);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD) {
            return;
        }

        if (sensorAccuracyChangeCallback == null) {
            return;
        }

        sensorAccuracyChangeCallback.apply(accuracy);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        synchronized (this) {
            if (locationChangeCallback == null) {
                return;
            }

            float latitude = (float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            float altitude = (float) location.getAltitude();
            long timeMillis = System.currentTimeMillis();
            GeomagneticField geomagneticField = new GeomagneticField(latitude, longitude, altitude, timeMillis);
            float declination = (float) Math.toRadians(geomagneticField.getDeclination());
            locationChangeCallback.apply(location, declination);
        }
    }

    public void registerListeners() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }
}

package pl.wilenskid.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassDataProvider implements SensorEventListener {

    private static final int MAX_ANGLE = 360;

    private final SensorManager sensorManager;
    private final Sensor accelerometerSensor;
    private final Sensor magneticFieldSensor;
    private final Vector gravityVector;
    private final Vector geomagneticVector;
    private final float[] rotationMatrix = new float[9];
    private final float[] inclinationMatrix = new float[9];
    private final AzimuthListener azimuthChangeListener;

    private float azimuth;

    public CompassDataProvider(Context context, AzimuthListener azimuthChangeListener) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.azimuthChangeListener = azimuthChangeListener;
        this.accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.gravityVector = new Vector();
        this.geomagneticVector = new Vector();
    }

    public void start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;

        synchronized (this) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravityVector.multiply(alpha).add(new Vector(sensorEvent.values).multiply(1 - alpha));
            }

            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagneticVector.multiply(alpha).add(new Vector(sensorEvent.values).multiply(1 - alpha));
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityVector.asArray(), geomagneticVector.asArray())) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                azimuth = (float) (Math.toDegrees(orientation[0]) + MAX_ANGLE) % MAX_ANGLE;

                if (azimuthChangeListener != null) {
                    azimuthChangeListener.onAzimuthChange(azimuth);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

}

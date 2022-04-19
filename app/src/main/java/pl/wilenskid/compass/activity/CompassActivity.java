package pl.wilenskid.compass.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.google.android.material.snackbar.Snackbar;
import pl.wilenskid.compass.*;
import pl.wilenskid.compass.callback.SensorAccuracyChangeCallback;
import pl.wilenskid.compass.factory.DirectionFactory;
import pl.wilenskid.compass.callback.AzimuthChangeCallback;
import pl.wilenskid.compass.callback.LocationChangeCallback;
import pl.wilenskid.compass.model.Direction;
import pl.wilenskid.compass.listener.CompassDataListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class CompassActivity extends AppCompatActivity {

    private static final float PIVOT_VALUE = 0.5f;

    private View compassLayout;
    private ImageView compassPointer;
    private DirectionFactory directionFactory;
    private TextView angleDirectionLabel;
    private TextView locationLabel;
    private TextView magneticFieldLabel;
    private TextView closestCityLabel;
    private ImageView sensorAccuracyIndicator;
    private CompassDataListener compassDataListener;
    private float currentAzimuth;
    private int currentMagneticFieldStrength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        compassLayout = findViewById(R.id.compassLayout);
        angleDirectionLabel = findViewById(R.id.angleDirectionLabel);
        compassPointer = findViewById(R.id.compassPointer);
        locationLabel = findViewById(R.id.locationLabel);
        magneticFieldLabel = findViewById(R.id.magneticFieldLabel);
        closestCityLabel = findViewById(R.id.closestCity);
        sensorAccuracyIndicator = findViewById(R.id.sensorAccuraccyIndicator);
        directionFactory = new DirectionFactory(this);
        compassDataListener = new CompassDataListener(this);
        compassDataListener.azimuthChangeCallback = getAzimuthChangeCallback();
        compassDataListener.sensorAccuracyChangeCallback = getSensorAccuracyCallback();
        compassDataListener.locationChangeCallback = getLocationChangeCallback();
        requestLocationPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        compassDataListener.registerListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassDataListener.registerListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassDataListener.unregisterListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compassDataListener.unregisterListeners();
    }

    private void updateCompassNeedle(float azimuth) {
        Animation animation = new RotateAnimation(-currentAzimuth, -azimuth, RELATIVE_TO_SELF, PIVOT_VALUE, RELATIVE_TO_SELF, PIVOT_VALUE);
        currentAzimuth = azimuth;
        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        compassPointer.startAnimation(animation);
    }

    private void updateAngleDirectionLabel(float azimuth) {
        int currentAngle = (int) azimuth;
        Direction direction = directionFactory.getDirection(currentAngle);
        String angleDirectionLabel = String.format(Locale.ENGLISH, "%d° %s", currentAngle, direction.getShortName());
        this.angleDirectionLabel.setText(angleDirectionLabel);
    }

    @SuppressLint("SetTextI18n")
    private void updateMagneticFieldLabel(float magneticFieldStrength) {
        if (currentMagneticFieldStrength == 0) {
            currentMagneticFieldStrength = Math.round(magneticFieldStrength);
        } else if (currentMagneticFieldStrength - magneticFieldStrength < 4.0f) {
            currentMagneticFieldStrength = Math.round(magneticFieldStrength);
        }

        magneticFieldLabel.setText(currentMagneticFieldStrength + " μT");
    }

    private void updateLocationLabel(Location location) {
        String positionTemplate = "%f, %f";
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        String position = String.format(Locale.ENGLISH, positionTemplate, longitude, latitude);
        locationLabel.setText(position);
    }

    private void updateDeclinationLabel(float declination) {
    }

    private void updateClosestCityLabel(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        String cityName = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            cityName = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        closestCityLabel.setText(cityName);
    }

    private void setSensorAccuracyIndicator(int sensorAccuracy) {
        switch (sensorAccuracy) {
            case 1:
                sensorAccuracyIndicator.setImageResource(R.drawable.ic_signal_1);
                return;
            case 2:
                sensorAccuracyIndicator.setImageResource(R.drawable.ic_signal_2);
                return;
            case 3:
                sensorAccuracyIndicator.setImageResource(R.drawable.ic_signal_3);
                return;
            default:
                sensorAccuracyIndicator.setImageResource(R.drawable.ic_signal_0);
        }
    }

    private void requestLocationPermissions() {
        if (shouldShowRequestPermissionRationale()) {
            return;
        }

        Snackbar.make(compassLayout, R.string.locationPermissionsRequest, Snackbar.LENGTH_SHORT).show();
        String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    private boolean shouldShowRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private AzimuthChangeCallback getAzimuthChangeCallback() {
        return (azimuth, magneticFieldStrength) -> runOnUiThread(() -> {
            updateCompassNeedle(azimuth);
            updateAngleDirectionLabel(azimuth);
            updateMagneticFieldLabel(magneticFieldStrength);
        });
    }

    private SensorAccuracyChangeCallback getSensorAccuracyCallback() {
        return sensorAccuracy -> runOnUiThread(() -> setSensorAccuracyIndicator(sensorAccuracy));
    }

    private LocationChangeCallback getLocationChangeCallback() {
        return (location, declination) -> runOnUiThread(() -> {
            updateLocationLabel(location);
            updateDeclinationLabel(declination);
            updateClosestCityLabel(location);
        });
    }
}
package pl.wilenskid.compass;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Locale;

public class CompassActivity extends AppCompatActivity {

    private ImageView compassPointer;
    private DirectionFactory directionFactory;
    private TextView angleDirectionLabel;
    private CompassDataProvider compassListener;
    private float currentAzimuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        angleDirectionLabel = findViewById(R.id.angleDirectionLabel);
        compassPointer = findViewById(R.id.compassPointer);
        directionFactory = new DirectionFactory(this);
        compassListener = new CompassDataProvider(this, getAzimuthListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        compassListener.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassListener.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        compassListener.stop();
    }

    private void moveCompassPointer(float azimuth) {
        Animation animation = new RotateAnimation(
                -currentAzimuth,
                -azimuth,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        currentAzimuth = azimuth;
        animation.setDuration(500);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        compassPointer.startAnimation(animation);
    }

    private void updateAngleAndDirectionLabel(float azimuth) {
        int currentAngle = (int) azimuth;
        Direction direction = directionFactory.getDirection(currentAngle);
        String angleDirectionLabel = String.format(Locale.ENGLISH, "%d %s", currentAngle, direction.getShortName());
        this.angleDirectionLabel.setText(angleDirectionLabel);
    }

    private AzimuthListener getAzimuthListener() {
        return azimuth -> runOnUiThread(() -> {
            moveCompassPointer(azimuth);
            updateAngleAndDirectionLabel(azimuth);
        });
    }
}
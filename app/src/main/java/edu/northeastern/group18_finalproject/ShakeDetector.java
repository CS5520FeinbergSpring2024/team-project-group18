package edu.northeastern.group18_finalproject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {
    // Minimum acceleration needed to register as a shake
    private static final float MIN_SHAKE_ACCELERATION = 4;
    // Minimum number of movements to register as a shake
    private static final int MIN_MOVEMENTS = 2;
    // Maximum time (in milliseconds) for the whole shake to occur
    private static final int MAX_SHAKE_DURATION = 200;

    private OnShakeListener listener;
    private long firstMovementTime = 0;
    private int movementCount = 0;

    public interface OnShakeListener {
        void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get sensor values
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calculate movement
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

        // Check if the acceleration is above the minimum threshold
        if (acceleration > MIN_SHAKE_ACCELERATION) {
            long now = System.currentTimeMillis();

            // Set the first movement time if it's the first movement
            if (firstMovementTime == 0) {
                firstMovementTime = now;
            }

            // Check total duration of the shake
            long elapsedTime = now - firstMovementTime;
            if (elapsedTime < MAX_SHAKE_DURATION) {
                movementCount++;

                // If enough movements have been registered, invoke listener
                if (movementCount >= MIN_MOVEMENTS) {
                    listener.onShake();
                    resetShakeParameters();
                }
            } else {
                resetShakeParameters();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for shake detection
    }

    private void resetShakeParameters() {
        firstMovementTime = 0;
        movementCount = 0;
    }
}


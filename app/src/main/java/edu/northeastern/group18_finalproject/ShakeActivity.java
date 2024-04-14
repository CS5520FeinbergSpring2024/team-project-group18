package edu.northeastern.group18_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ShakeActivity extends AppCompatActivity {
    private ShakeDetector shakeDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        // Initialize ShakeDetector
        shakeDetector = new ShakeDetector();
        // Register ShakeDetector with the accelerometer sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                Intent intent = new Intent(ShakeActivity.this, DisplayRecipe.class);
                startActivity(intent);
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("MenuItemClicked", "Clicked item ID: " + item.getItemId());
                if (item.getItemId() == R.id.invisible_item) {
                    return false;
                } else if (item.getItemId() == R.id.uploadRecipe) {
                    // Handle upload recipe click
                    uploadRecipe();
                    return true;
                } else if (item.getItemId() == R.id.friendList) {
                    // Handle friend list click
                    openFriendList();
                    return true;
                } else {
                    return false;
                }
            }
        });

    }

    private void uploadRecipe() {
        Intent intent = new Intent(ShakeActivity.this, UploadRecipeActivity.class);
        startActivity(intent);
    }

    private void openFriendList() {
        startActivity(new Intent(ShakeActivity.this, FriendListActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register ShakeDetector with the accelerometer sensor
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister ShakeDetector when the activity is paused
        if (sensorManager != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle the back press event
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

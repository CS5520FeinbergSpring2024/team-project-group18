package edu.northeastern.group18_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

public class ShakeActivity extends AppCompatActivity {
    private ShakeDetector shakeDetector;
    private Button friendListButton;
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

        ImageButton uploadRecipeButton = findViewById(R.id.uploadRecipeButton);
        uploadRecipeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ShakeActivity.this, UploadRecipeActivity.class);
                startActivity(intent);
            }
        });

        friendListButton = findViewById(R.id.friendListButton);
        friendListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the FriendListActivity
//                startActivity(new Intent(ShakeActivity.this, FriendListActivity.class));
                try {
                    // Start the FriendListActivity
                    startActivity(new Intent(ShakeActivity.this, FriendListActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "Error starting FriendListActivity: " + e.getMessage());
                }
            }
        });
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

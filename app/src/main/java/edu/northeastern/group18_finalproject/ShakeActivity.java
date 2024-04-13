package edu.northeastern.group18_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        // Initialize ShakeDetector
        shakeDetector = new ShakeDetector();
        // Register ShakeDetector with the accelerometer sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                // Perform action when shake is detected
                Log.d("Detected!", "detected!!!!!!");
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
    public void onBackPressed() {
        // Handle the back press event
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

package edu.northeastern.group18_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "MyChannel";
    private TextInputEditText usernameEditText;
    private DatabaseReference usersRef;
    private boolean initialized = false;
    private Long oldCounter;
    DatabaseReference userMessagesRef;
    DatabaseReference friendMessagesRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usernameEditText = findViewById(R.id.usernameText);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton signupButton = findViewById(R.id.signupButton);
        loginButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.iconBlue)));
        signupButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.iconBlue)));
        // Notify user to open notification
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupUser();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
    private void loginUser() {
        final String username = usernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserSession.setUsername(username);
                    userMessagesRef = usersRef.child(username).child("receiveMessageInfoMap");
                    addmessageListener();
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ShakeActivity.class); /*DisplayRecipe ShakeActivity*/
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Account does not exist. Please sign up", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addmessageListener(){
        userMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("listened", "lisntedned!!!");
                    if (!initialized) {
                        oldCounter = dataSnapshot.child("counter").getValue(Long.class);
                        initialized = true;
                    }
                    if (!oldCounter.equals(dataSnapshot.child("counter").getValue(Long.class))) {
                        Log.d("newly", "added!!!");
                    /*
                    Mengyuan & Yushi: send notification if new element added
                     */
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.shake)
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.shake))
                                .setContentTitle("New Message Received!")
                                .setContentText("You received a new message from a friend" + dataSnapshot.child("sender").getValue(String.class))
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);
                        long currentTimeMillis = System.currentTimeMillis();
                        int notificationId = (int) currentTimeMillis & Integer.MAX_VALUE;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            CharSequence name = "My Channel";
                            String description = "This is my channel";
                            int importance = NotificationManager.IMPORTANCE_DEFAULT;
                            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                            channel.setDescription(description);
                            // Register the channel with the system
                            NotificationManager notificationManager = getSystemService(NotificationManager.class);
                            notificationManager.createNotificationChannel(channel);

                            notificationManager.notify(notificationId, builder.build());
                        }
                    }
                } else {
                    Map<String, Object> receiveMessageInfoMap = new HashMap<>();
                    receiveMessageInfoMap.put("counter", 0);
                    receiveMessageInfoMap.put("sender", "");
                    userMessagesRef.setValue(receiveMessageInfoMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Counter", "Counter created and initialized successfully!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Counter", "Failed to create and initialize counter: " + e.getMessage());
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signupUser() {
        final String username = usernameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    Toast.makeText(MainActivity.this, "Account already exists. Please login", Toast.LENGTH_SHORT).show();
                } else {
                    // Username does not exist, create new
                    // might need more field in the future
                    usersRef.child(username).child("username").setValue(username);
                    UserSession.setUsername(username);
                    Toast.makeText(MainActivity.this, "Account created. You are now logged in", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, ShakeActivity.class); /*DisplayRecipe ShakeActivity*/
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

package edu.northeastern.group18_finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private DatabaseReference usersRef;


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

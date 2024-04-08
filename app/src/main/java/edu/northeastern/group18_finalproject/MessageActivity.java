package edu.northeastern.group18_finalproject;


import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class MessageActivity extends AppCompatActivity {

    private TextView messageTextview;
    private TextView senderTextView;
    private EditText messageEditText;
    private Button sendMessageButton;

    private DatabaseReference messagesRef;
    private String currentUsername;
    private String friendUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize views
        messageTextview = findViewById(R.id.messageTextView);
        senderTextView = findViewById(R.id.senderTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);


        // Get friend user ID from intent
        // Need to import from friendlist activity
        friendUsername = getIntent().getStringExtra("friendUserId");

        currentUsername = UserSession.getUsername();

        // Initialize Firebase Database reference
        messagesRef = FirebaseDatabase.getInstance().getReference().child("user").child("message").child(friendUsername);

        // Set up onClickListener for send message button
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Listen for new messages
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String message = dataSnapshot.child("currentMessage").getValue(String.class);
                messageTextview.setText(message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            // Implement other methods of ChildEventListener as needed
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
//            Message message = new Message(currentUsername, friendUsername, messageText, System.currentTimeMillis());

            messagesRef.child("currentMessage").setValue(messageText);
            messagesRef.child("sender").setValue(currentUsername);

            messageEditText.setText("");
        }
    }
}



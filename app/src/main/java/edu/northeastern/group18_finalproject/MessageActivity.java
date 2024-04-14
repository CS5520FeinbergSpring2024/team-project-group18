package edu.northeastern.group18_finalproject;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private TextView messageTextview;
    private TextView senderTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private DatabaseReference messagesRef;
    private DatabaseReference friendMessagesRef;
    private DatabaseReference senderInfoRef;
    private String currentUsername;
    private String friendUsername;
    private Long counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize views
        messageTextview = findViewById(R.id.messageTextView);
        senderTextView = findViewById(R.id.senderTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        friendUsername = getIntent().getStringExtra("friendUsername");

        currentUsername = UserSession.getUsername();

        messagesRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUsername).child("message").child(friendUsername);
        friendMessagesRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendUsername).child("message").child(currentUsername);
        getCounter();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String message = dataSnapshot.child("currentMessage").getValue(String.class);
                String sender = dataSnapshot.child("sender").getValue(String.class);
                messageTextview.setText(message);
                senderTextView.setText(sender);

                if (message != null && message.contains("recipeId")) {
                    String recipeId = extractRecipeId(message);
                    if (recipeId != null) {
                        messageTextview.setOnClickListener(v -> {
                        Intent displayIntent = new Intent(MessageActivity.this, DisplayRecipe.class);
                        displayIntent.putExtra("recipeId", recipeId);
                        startActivity(displayIntent);
                    });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private String extractRecipeId(String message) {
        String prefix = "recipeId=";
        int startIndex = message.indexOf(prefix);
        if (startIndex != -1) {
            return message.substring(startIndex + prefix.length()).trim();
        }
        return null;
    }


    private void getCounter(){
        // get Counter for received message person
        senderInfoRef = FirebaseDatabase.getInstance().getReference().child("users").child(friendUsername).child("receiveMessageInfoMap");
        senderInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Check whether counter exist. If so, get it and increase value
                    Map<String, Object> receiveMessageInfoMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (receiveMessageInfoMap != null && receiveMessageInfoMap.containsKey("counter") && receiveMessageInfoMap.containsKey("sender")) {
                        counter = (Long) receiveMessageInfoMap.get("counter");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
//            Message message = new Message(currentUsername, friendUsername, messageText, System.currentTimeMillis());

            messagesRef.child("currentMessage").setValue(messageText);
            messagesRef.child("sender").setValue(currentUsername);
            friendMessagesRef.child("currentMessage").setValue(messageText);
            friendMessagesRef.child("sender").setValue(currentUsername);
            senderTextView.setText(currentUsername);
            messageTextview.setText(messageText);

            messageEditText.setText("");


            // Update Counter
            counter++;
            Map<String, Object> receiveMessageInfoMap = new HashMap<>();
            receiveMessageInfoMap.put("counter", counter);
            receiveMessageInfoMap.put("sender", currentUsername);
            senderInfoRef.setValue(receiveMessageInfoMap);
        }
    }
}



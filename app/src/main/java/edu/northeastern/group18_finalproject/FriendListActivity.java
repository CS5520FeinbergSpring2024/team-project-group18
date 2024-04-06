package edu.northeastern.group18_finalproject;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private RecyclerView friendListRecyclerView;
    private List<String> friendList;
    private DatabaseReference usersRef;
    private FriendListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        friendListRecyclerView = findViewById(R.id.friendListRecyclerView);

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Fetch the list of friends for the current user
        fetchFriendList();

        // Set up RecyclerView adapter
        friendListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void fetchFriendList() {
        String currentUsername = UserSession.getUsername();
        DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
        currentUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    friendList = new ArrayList<>();
                    for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                        String friendUsername = friendSnapshot.getValue(String.class);
                        friendList.add(friendUsername);
                        Log.d("friend", friendUsername);
                        Log.d("friend", friendList.toString());
                    }
                    // Update the RecyclerView adapter with the fetched friend list
                    adapter = new FriendListAdapter(friendList);
                    friendListRecyclerView.setAdapter(adapter);

                } else {
                    Log.d("Firebase", "No friends found for the user");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error fetching friend list: " + databaseError.getMessage());
            }
        });
    }

}



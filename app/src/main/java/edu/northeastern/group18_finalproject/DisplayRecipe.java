package edu.northeastern.group18_finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class DisplayRecipe extends AppCompatActivity {

    private TextView recipeNameTextView;
    private TextView recipeCreatorTextView;
    private TextView recipeDescriptionTextView;
    private TextView recipeTagsTextView;
    private RecyclerView recipeImagesRecyclerView;
    private DatabaseReference recipesRef;

    private DatabaseReference usersRef;
//    private User currentUser;
//    private User recipeCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recipe);

        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        recipeCreatorTextView = findViewById(R.id.recipeCreatorTextView);
        recipeDescriptionTextView = findViewById(R.id.recipeDescriptionTextView);
        recipeTagsTextView = findViewById(R.id.recipeTagsTextView);
        recipeImagesRecyclerView = findViewById(R.id.recipeImagesRecyclerView);

        recipesRef = FirebaseDatabase.getInstance().getReference().child("recipes");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Fetch and display the first recipe from the database
        fetchFirstRecipe();

        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(recipeCreatorTextView.getText().toString());
            }
        });
    }


    private void fetchFirstRecipe() {
        recipesRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot firstRecipeSnapshot = dataSnapshot.getChildren().iterator().next();

                    String recipeId = firstRecipeSnapshot.getKey();
                    String recipeName = firstRecipeSnapshot.child("name").getValue(String.class);
                    String recipesCreator = firstRecipeSnapshot.child("creator").getValue(String.class);
                    String recipeDescription = firstRecipeSnapshot.child("description").getValue(String.class);

                    List<String> tags = new ArrayList<>();
                    for (DataSnapshot tagSnapshot : firstRecipeSnapshot.child("tags").getChildren()) {
                       tags.add(tagSnapshot.getValue().toString());
//                        String tag = tagSnapshot.getValue(String.class);
//                        tags.add(tag);
                    }

                    List<String> imageUrls = new ArrayList<>();
                    for (DataSnapshot imageSnapshot : firstRecipeSnapshot.child("imageUrl").getChildren()) {
                        imageUrls.add(imageSnapshot.getValue().toString());
//                        String imageUrl = imageSnapshot.getValue(String.class);
//                        imageUrls.add(imageUrl);

                    }

                    Recipe firstRecipe = new Recipe(recipeId, recipeName, recipesCreator, recipeDescription, tags, imageUrls);
                    populateUIWithRecipe(firstRecipe);
                } else {
                    Toast.makeText(DisplayRecipe.this, "No recipes found in the database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching recipe data: " + databaseError.getMessage());
            }
        });
    }

    private void populateUIWithRecipe(Recipe recipe) {
        recipeNameTextView.setText(recipe.getName());
        recipeCreatorTextView.setText(recipe.getCreator());
        recipeDescriptionTextView.setText(recipe.getDescription());

        recipeTagsTextView.setText(TextUtils.join(", ", recipe.getTags()));

        // Set up RecyclerView for recipe images
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recipeImagesRecyclerView.setLayoutManager(layoutManager);
        RecipeImageAdapter adapter = new RecipeImageAdapter(recipe.getImageUrl());
        recipeImagesRecyclerView.setAdapter(adapter);
    }

    private void addFriend(String creatorUsername) {
        String currentUsername = UserSession.getUsername();
        DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
        currentUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (!dataSnapshot.getChildren().iterator().hasNext() || !dataSnapshot.getChildren().iterator().next().getValue(String.class).equals(creatorUsername)) {
                        // If not already friends, add the creator as a friend
                        currentUserFriendsRef.push().setValue(creatorUsername);
                        updateFriendButtonUI();
                    } else {
                        Toast.makeText(DisplayRecipe.this, "You are already friends with the recipe creator", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    currentUserFriendsRef.push().setValue(creatorUsername);
                    updateFriendButtonUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error checking friends list: " + databaseError.getMessage());
            }
        });

    }

    private void updateFriendButtonUI() {
        // Update the UI to reflect the new friend
        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setText("Friend Added");
        addFriendButton.setEnabled(false); // Disable the button after the user has added the friend
    }
}

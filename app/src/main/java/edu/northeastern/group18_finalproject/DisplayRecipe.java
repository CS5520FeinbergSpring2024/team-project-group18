package edu.northeastern.group18_finalproject;

import android.content.Intent;
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

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DisplayRecipe extends AppCompatActivity {

    private TextView recipeNameTextView;
    private TextView recipeCreatorTextView;
    private TextView recipeDescriptionTextView;
    private TextView recipeTagsTextView;
    private TextView recipeCookingTimeTextView,recipeIngredientsTextView, recipeDirectionsTextView ;
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
        recipeCookingTimeTextView = findViewById(R.id.recipeCookingTimeTextView);
        recipeIngredientsTextView = findViewById(R.id.recipeIngredientsTextView);
        recipeDirectionsTextView = findViewById(R.id.recipeDirectionsTextView);

        recipeTagsTextView = findViewById(R.id.recipeTagsTextView);
        recipeImagesRecyclerView = findViewById(R.id.recipeImagesRecyclerView);

        recipesRef = FirebaseDatabase.getInstance().getReference().child("recipes");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Fetch and display the first recipe from the database

        String recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId != null) {
            fetchRecipeById(recipeId);

        } else {
            fetchRecipeCountAndRandomRecipe();
//            fetchFirstRecipe();
        }

        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(recipeCreatorTextView.getText().toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Handle the back press event
        super.onBackPressed();
        Intent intent = new Intent(this, ShakeActivity.class);
        startActivity(intent);
        finish();
    }

    private void fetchRecipeById(String recipeId) {
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);

                    if (recipe != null) {
                        populateUIWithRecipe(recipe);
                    } else {
                        Toast.makeText(DisplayRecipe.this, "Failed to load recipe details.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DisplayRecipe.this, "Recipe does not exist.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DisplayRecipe.this, "Error fetching recipe: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipeCountAndRandomRecipe() {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if (count > 0) {
                    int randomIndex = new Random().nextInt((int) count);
                    fetchRandomRecipe(randomIndex);
                } else {
                    Toast.makeText(DisplayRecipe.this, "No recipes found in the database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DisplayRecipe.this, "Error fetching recipe count: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRandomRecipe(final int randomIndex) {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int currentIndex = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (currentIndex == randomIndex) {
                        Recipe recipe = snapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            populateUIWithRecipe(recipe);
                            break;
                        }
                    }
                    currentIndex++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DisplayRecipe.this, "Error fetching random recipe: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }





//    private void fetchFirstRecipe() {
//        recipesRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    DataSnapshot firstRecipeSnapshot = dataSnapshot.getChildren().iterator().next();
//
//                    String recipeId = firstRecipeSnapshot.getKey();
//                    String recipeName = firstRecipeSnapshot.child("name").getValue(String.class);
//                    String recipesCreator = firstRecipeSnapshot.child("creator").getValue(String.class);
//                    String recipeDescription = firstRecipeSnapshot.child("description").getValue(String.class);
//
//                    List<String> tags = new ArrayList<>();
//                    for (DataSnapshot tagSnapshot : firstRecipeSnapshot.child("tags").getChildren()) {
//                       tags.add(tagSnapshot.getValue().toString());
////                        String tag = tagSnapshot.getValue(String.class);
////                        tags.add(tag);
//                    }
//
//                    List<String> imageUrls = new ArrayList<>();
//                    for (DataSnapshot imageSnapshot : firstRecipeSnapshot.child("imageUrl").getChildren()) {
//                        imageUrls.add(imageSnapshot.getValue().toString());
////                        String imageUrl = imageSnapshot.getValue(String.class);
////                        imageUrls.add(imageUrl);
//
//                    }
//
//                    Recipe firstRecipe = new Recipe(recipeId, recipeName, recipesCreator, recipeDescription, tags, imageUrls);
//                    populateUIWithRecipe(firstRecipe);
//                } else {
//                    Toast.makeText(DisplayRecipe.this, "No recipes found in the database", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("Firebase", "Error fetching recipe data: " + databaseError.getMessage());
//            }
//        });
//    }

    private void populateUIWithRecipe(Recipe recipe) {
        recipeNameTextView.setText(recipe.getName());
        recipeCreatorTextView.setText("Posted by: " + recipe.getCreator());
        recipeDescriptionTextView.setText(recipe.getDescription());
        recipeCookingTimeTextView.setText(recipe.getCookingTime());
        recipeIngredientsTextView.setText(recipe.getIngredients());
        recipeDescriptionTextView.setText(recipe.getDescription());
        recipeTagsTextView.setText(TextUtils.join(", ", recipe.getTags()));

        // Set up RecyclerView for recipe images

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recipeImagesRecyclerView.setLayoutManager(layoutManager);
            RecipeImageAdapter adapter = new RecipeImageAdapter(recipe.getImageUrl());
            recipeImagesRecyclerView.setAdapter(adapter);
            recipeImagesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            recipeImagesRecyclerView.setVisibility(View.GONE);
        }
    }

    private void addFriend(String creatorUsername) {
        String currentUsername = UserSession.getUsername();
        DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
        DatabaseReference creatorUserFriendsRef = usersRef.child(creatorUsername).child("friends");
        currentUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (!dataSnapshot.getChildren().iterator().hasNext() || !dataSnapshot.getChildren().iterator().next().getValue(String.class).equals(creatorUsername)) {
                        // If not already friends, add the creator as a friend
                        currentUserFriendsRef.push().setValue(creatorUsername);
                        creatorUserFriendsRef.push().setValue(currentUsername);
                        updateFriendButtonUI();
                    } else {
                        Toast.makeText(DisplayRecipe.this, "You are already friends with the recipe creator", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    currentUserFriendsRef.push().setValue(creatorUsername);
                    creatorUserFriendsRef.push().setValue(currentUsername);
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

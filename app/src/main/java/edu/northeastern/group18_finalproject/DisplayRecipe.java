package edu.northeastern.group18_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DisplayRecipe extends AppCompatActivity {
    private TextView recipeNameTextView;
    private TextView recipeCreatorTextView;
    private TextView recipeDescriptionTextView;
    private TextView recipeTagsTextView;
    private TextView recipeCookingTimeTextView,recipeIngredientsTextView, recipeDirectionsTextView ;

    private ViewPager2 recipeImagesView;
    private TabLayout tabLayout;

    private DatabaseReference recipesRef;

    private DatabaseReference usersRef;
    private int currentImagePosition = 0;
    private List<String> imgUrls;
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
        tabLayout = findViewById(R.id.viewPagerIndicator);

        recipeImagesView = findViewById(R.id.recipeImagesView);

        recipesRef = FirebaseDatabase.getInstance().getReference().child("recipes");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        if (savedInstanceState != null) {
            // Restore state from savedInstanceState
            restore(savedInstanceState);
        } else {
            // Fetch and display the first recipe from the database
            String recipeId = getIntent().getStringExtra("recipeId");
            if (recipeId != null) {
                fetchRecipeById(recipeId);
            } else {
                fetchRandomRecipe();
            }
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

    private void fetchRandomRecipe() {
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null && !recipe.getCreator().equals(UserSession.getUsername())) {
                        recipes.add(recipe);
                    }
                }
                if (!recipes.isEmpty()) {
                    int randomIndex = new Random().nextInt(recipes.size());
                    Recipe randomRecipe = recipes.get(randomIndex);
                    populateUIWithRecipe(randomRecipe);
                } else {
                    Toast.makeText(DisplayRecipe.this, "No recipes found that were not posted by you", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DisplayRecipe.this, "Error fetching random recipe: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void populateUIWithRecipe(Recipe recipe) {
        recipeNameTextView.setText(recipe.getName());
        recipeDescriptionTextView.setText(recipe.getDescription());
        recipeCookingTimeTextView.setText(recipe.getCookingTime());
        recipeIngredientsTextView.setText(recipe.getIngredients());
        recipeDescriptionTextView.setText(recipe.getDescription());

        if (recipe.getCreator() == null || recipe.getCreator().isEmpty()) {
            recipeCreatorTextView.setText("Anonymous");
        } else {
            recipeCreatorTextView.setText(recipe.getCreator());
        }


        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            imgUrls = recipe.getImageUrl();
            RecipeImageAdapter adapter = new RecipeImageAdapter(recipe.getImageUrl());
            recipeImagesView.setAdapter(adapter);
            recipeImagesView.setVisibility(View.VISIBLE);

            new TabLayoutMediator(tabLayout, recipeImagesView,
                    (tab, position) -> {
                        currentImagePosition = position;
                    }).attach();

        } else {
            recipeImagesView.setVisibility(View.GONE);
        }

        if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
            recipeTagsTextView.setText(TextUtils.join(", ", recipe.getTags()));
        } else{
            recipeTagsTextView.setVisibility(View.GONE);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("recipeName", recipeNameTextView.getText().toString());
        outState.putString("creatorName", recipeCreatorTextView.getText().toString());
        outState.putString("Description", recipeDescriptionTextView.getText().toString());
        outState.putString("cookTime", recipeCookingTimeTextView.getText().toString());
        outState.putString("ingredient", recipeIngredientsTextView.getText().toString());
        outState.putString("direction", recipeDirectionsTextView.getText().toString());
        outState.putString("tags", recipeTagsTextView.getText().toString());

        if (imgUrls != null) {
            String[] imgUrlsArray = new String[imgUrls.size()];
            imgUrlsArray = imgUrls.toArray(imgUrlsArray);
            outState.putStringArray("imgUrlsArray", imgUrlsArray);
            outState.putInt("currentImagePosition", currentImagePosition);
        } else {
            outState.putStringArray("imgUrlsArray", null);
            outState.putInt("currentImagePosition", -1);
        }

    }

    private void restore(Bundle inState){
        recipeNameTextView.setText(inState.getString("recipeName"));
        recipeCreatorTextView.setText(inState.getString("creatorName"));
        recipeDescriptionTextView.setText(inState.getString("Description"));
        recipeCookingTimeTextView.setText(inState.getString("cookTime"));
        recipeIngredientsTextView.setText(inState.getString("ingredient"));
        recipeDirectionsTextView.setText(inState.getString("direction"));
        recipeTagsTextView.setText(inState.getString("tags"));
        String[] imgUrlsArray = inState.getStringArray("imgUrlsArray");
        if (imgUrlsArray != null) {
            imgUrls = Arrays.asList(imgUrlsArray);
            currentImagePosition = inState.getInt("currentImagePosition");
            RecipeImageAdapter adapter = new RecipeImageAdapter(imgUrls);
            recipeImagesView.setAdapter(adapter);
            recipeImagesView.setVisibility(View.VISIBLE);

            new TabLayoutMediator(tabLayout, recipeImagesView,
                    (tab, position) -> {
                        currentImagePosition = position;
                    }).attach();
        } else {
            recipeImagesView.setVisibility(View.GONE);
        }
    }

}

//    private void setupShareButton(){
//        ImageButton shareRecipeButton = findViewById(R.id.shareRecipeButton);
//        shareRecipeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentRecipe != null && currentRecipe.getRecipeId() != null) {
//                    shareRecipe(currentRecipe.getRecipeId());
//                } else {
//                    Toast.makeText(DisplayRecipe.this, "No recipe loaded to share.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//    }
//
//    public void shareRecipe(String recipeId) {
//
//        String url = "https://www.shakerecipe.com/recipe?id=" + recipeId;
//
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this recipe! " + url);
//        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"));
//    }


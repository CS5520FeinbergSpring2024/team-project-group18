package edu.northeastern.group18_finalproject;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
    private Recipe currentRecipe;
    private ImageButton likeRecipeButton;
    private TextView likeCountTextView;

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

        likeRecipeButton = findViewById(R.id.likeRecipeButton);
        likeCountTextView = findViewById(R.id.likeCountTextView);

        if (savedInstanceState != null) {
            // Restore state from savedInstanceState
            restore(savedInstanceState);

            String recipeId = savedInstanceState.getString("recipeId");
            if (recipeId != null) {
                fetchRecipeById(recipeId);
            }

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
                addFriendButton.setBackgroundColor(getResources().getColor(R.color.button_ripple_color));
            }
        });

        likeRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleLike(currentRecipe);
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
                        currentRecipe = recipe;
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
                    currentRecipe = randomRecipe;
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
            String tagsWithHash = recipe.getTags().stream()
                    .map(tag -> "#" + tag) // Add "#" to each tag
                    .collect(Collectors.joining(", ")); // Join them with a comma
            recipeTagsTextView.setText(tagsWithHash);
        } else{
            recipeTagsTextView.setVisibility(View.GONE);
        }

        updateLikeButtonState(recipe);
        setupLikeEventListener(recipe);
//        setupLikeCountListener(recipe);
        setupShareButton();

    }

    private void addFriend(String creatorUsername) {
        String currentUsername = UserSession.getUsername();
        DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
        DatabaseReference creatorUserFriendsRef = usersRef.child(creatorUsername).child("friends");
        currentUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("snapShopt exists", "yes!!!");
                    boolean isAlreadyFriend = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String friendName = snapshot.getValue(String.class);
                        if (friendName != null && friendName.equals(creatorUsername)) {
                            isAlreadyFriend = true;
                            break;
                        }
                    }

                    if (!isAlreadyFriend) {
                        Log.d("friend exists", "no!!!");
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

    private void setupShareButton(){
        ImageButton shareRecipeButton = findViewById(R.id.shareRecipeButton);
        shareRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentRecipe != null && currentRecipe.getRecipeId() != null) {
                    shareRecipe(currentRecipe.getRecipeId());
                } else {
                    Toast.makeText(DisplayRecipe.this, "No recipe loaded to share.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void shareRecipe(String recipeId) {
        String message = "recipeId=" + recipeId;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this recipe!\n" + message);
        startActivity(Intent.createChooser(shareIntent, "Share Recipe via"));
    }

    private void toggleLike(Recipe recipe){
        DatabaseReference recipeRef = recipesRef.child(recipe.getRecipeId());
        DatabaseReference userLikesRef = recipeRef.child("likedBy").child(UserSession.getUsername());
        DatabaseReference likesRef = recipeRef.child("likes");

        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean liked = dataSnapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(liked)) {
                    userLikesRef.removeValue();
                    likesRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentLikes = currentData.getValue(Integer.class);
                            if (currentLikes != null && currentLikes > 0) {
                                currentData.setValue(currentLikes - 1);
                            } else {
                                currentData.setValue(0);
                            }
                            return Transaction.success(currentData);
                        }
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                        }
                    });
                } else {
                    userLikesRef.setValue(true);
                    likesRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer currentLikes = currentData.getValue(Integer.class);
                            if (currentLikes == null) {
                                currentData.setValue(1);
                            } else {
                                currentData.setValue(currentLikes + 1);
                            }
                            return Transaction.success(currentData);
                        }
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DisplayRecipe", "Failed to read like status", databaseError.toException());
            }
        });

//        Boolean liked = recipe.getLikedBy().get(UserSession.getUsername());
//        if (liked != null && liked){
//            recipeRef.child("likes").setValue(recipe.getLikes() - 1);
//            userLikesRef.removeValue();
//            likeRecipeButton.setImageResource(R.drawable.ic_likebutton);
//
//        } else {
//            recipeRef.child("likes").setValue(recipe.getLikes() + 1);
//            userLikesRef.setValue(true);
//            likeRecipeButton.setImageResource(R.drawable.ic_filled_like_button);
//        }
    }

    private void updateLikeButtonState(Recipe recipe) {
        if (recipe.getLikedBy().containsKey(UserSession.getUsername()) && recipe.getLikedBy().get(UserSession.getUsername())) {
            likeRecipeButton.setImageResource(R.drawable.ic_filled_like_button);;
        } else {
            likeRecipeButton.setImageResource(R.drawable.ic_likebutton);;
        }
        likeCountTextView.setText(String.valueOf(recipe.getLikes()));
    }

    private void setupLikeEventListener(Recipe recipe) {
        DatabaseReference likeRef = recipesRef.child(recipe.getRecipeId()).child("likedBy");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean liked = dataSnapshot.child(UserSession.getUsername()).getValue(Boolean.class);
                likeRecipeButton.setImageResource(liked != null && liked ? R.drawable.ic_filled_like_button : R.drawable.ic_likebutton);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DisplayRecipe", "Failed to listen for like changes", databaseError.toException());
            }
        });
    }

    private void setupLikeCountListener(Recipe recipe) {
        DatabaseReference likesRef = recipesRef.child(recipe.getRecipeId()).child("likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer likes = dataSnapshot.getValue(Integer.class);
                if (likes != null) {
                    likeCountTextView.setText(String.valueOf(likes));
                } else {
                    likeCountTextView.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DisplayRecipe", "Error fetching like count", databaseError.toException());
            }
        });
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

        if (currentRecipe != null) {
            outState.putString("recipeId", currentRecipe.getRecipeId());
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


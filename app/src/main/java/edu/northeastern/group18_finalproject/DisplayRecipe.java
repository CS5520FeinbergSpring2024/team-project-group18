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

import com.google.android.material.button.MaterialButton;
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
    private MaterialButton addFriendButton;


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

        addFriendButton = findViewById(R.id.addFriendButton);

        if (savedInstanceState != null) {
            String recipeId = savedInstanceState.getString("recipeId");
            if (recipeId != null) {
                fetchRecipeById(recipeId);
            }

        } else {
            String recipeId = getIntent().getStringExtra("recipeId");
            if (recipeId != null) {
                fetchRecipeById(recipeId);
            } else {
                fetchRandomRecipe();
            }
        }

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
        recipeDirectionsTextView.setText(recipe.getDirections());

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
                    .map(tag -> "#" + tag)
                    .collect(Collectors.joining(" "));
            recipeTagsTextView.setText(tagsWithHash);
        } else{
            recipeTagsTextView.setVisibility(View.GONE);
        }

        updateLikeButtonState(recipe);
        setupLikeEventListener(recipe);
        setupLikeCountListener(recipe);
        setupFriendButtonUI(recipe.getCreator());
        setupShareButton();

    }

    private void setupFriendButtonUI(String creatorUsername) {
        String currentUsername = UserSession.getUsername();
        if (currentUsername.equals(creatorUsername)) {
            addFriendButton.setVisibility(View.GONE);
        } else {
            addFriendButton.setEnabled(false);

            DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
            currentUserFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isAlreadyFriend = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String friendName = snapshot.getValue(String.class);
                        if (friendName != null && friendName.equals(creatorUsername)) {
                            isAlreadyFriend = true;
                            break;
                        }
                    }
                    if (isAlreadyFriend) {
                        addFriendButton.setText("Connected");
                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.button_ripple_color));
                    } else {
                        addFriendButton.setText("Add Friend");
                        addFriendButton.setEnabled(true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("DisplayRecipe", "Error checking friends list");
                }
            });
        }
    }


    private void addFriend(String creatorUsername) {
        String currentUsername = UserSession.getUsername();
        DatabaseReference currentUserFriendsRef = usersRef.child(currentUsername).child("friends");
        DatabaseReference creatorUserFriendsRef = usersRef.child(creatorUsername).child("friends");

        currentUserFriendsRef.push().setValue(creatorUsername).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                creatorUserFriendsRef.push().setValue(currentUsername).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        addFriendButton.setText("Connected");
                        addFriendButton.setEnabled(false);
                        Toast.makeText(DisplayRecipe.this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("AddFriendError", "2nd Task Failed: " + task2.getException().getMessage());
                    }
                });
            } else {
                Log.d("AddFriendError", "1st Task Failed: " + task.getException().getMessage());
            }
        });
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

        if (currentRecipe != null) {
            outState.putString("recipeId", currentRecipe.getRecipeId());
        }

    }
}


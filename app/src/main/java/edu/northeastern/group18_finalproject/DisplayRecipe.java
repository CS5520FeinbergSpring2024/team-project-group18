package edu.northeastern.group18_finalproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
    private TextView recipeDescriptionTextView;
    private TextView recipeTagsTextView;
    private RecyclerView recipeImagesRecyclerView;
    private DatabaseReference recipesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recipe);

        // Initialize views
        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        recipeDescriptionTextView = findViewById(R.id.recipeDescriptionTextView);
        recipeTagsTextView = findViewById(R.id.recipeTagsTextView);
        recipeImagesRecyclerView = findViewById(R.id.recipeImagesRecyclerView);

        // Get reference to the "recipes" node in the database
        recipesRef = FirebaseDatabase.getInstance().getReference().child("recipes");

        // Fetch and display the first recipe from the database
        fetchFirstRecipe();
    }

    private void fetchFirstRecipe() {
        recipesRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot firstRecipeSnapshot = dataSnapshot.getChildren().iterator().next();

                    String recipeId = firstRecipeSnapshot.getKey();
                    String recipeName = firstRecipeSnapshot.child("name").getValue(String.class);
                    String recipeDescription = firstRecipeSnapshot.child("description").getValue(String.class);

                    // Fetch tags
                    List<String> tags = new ArrayList<>();
                    for (DataSnapshot tagSnapshot : firstRecipeSnapshot.child("tags").getChildren()) {
                        tags.add(tagSnapshot.getKey());
                    }

                    // Fetch images
                    List<String> imageUrls = new ArrayList<>();
                    for (DataSnapshot imageSnapshot : firstRecipeSnapshot.child("images").getChildren()) {
                        imageUrls.add(imageSnapshot.getKey());
                    }

                    // Create Recipe object for the first recipe
                    Recipe firstRecipe = new Recipe(recipeId, recipeName, recipeDescription, tags, imageUrls);

                    // Populate the UI with the first recipe
                    populateUIWithRecipe(firstRecipe);
                } else {
                    // Handle the case where no recipes are found in the database
                    Toast.makeText(DisplayRecipe.this, "No recipes found in the database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the database query
                Log.e("Firebase", "Error fetching recipe data: " + databaseError.getMessage());
            }
        });
    }

    private void populateUIWithRecipe(Recipe recipe) {
        recipeNameTextView.setText(recipe.getName());
        recipeDescriptionTextView.setText(recipe.getDescription());
        recipeTagsTextView.setText(TextUtils.join(", ", recipe.getTags()));

        // Set up RecyclerView for recipe images
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recipeImagesRecyclerView.setLayoutManager(layoutManager);
        RecipeImageAdapter adapter = new RecipeImageAdapter(recipe.getImageUrl());
        recipeImagesRecyclerView.setAdapter(adapter);
    }
}

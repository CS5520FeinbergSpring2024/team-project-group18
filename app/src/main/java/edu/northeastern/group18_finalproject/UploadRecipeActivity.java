package edu.northeastern.group18_finalproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UploadRecipeActivity extends AppCompatActivity {

    private ChipGroup chipGroupTags;
    private ImageButton addTagButton, deletePhotoButton;

    private Button addPhotoButton, postRecipeButton;

    private ImageView photoImageView;

    private TextInputEditText recipeTitle, recipeDescription, cookingTime, ingredients, directions;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    static final int REQUEST_IMAGE_CAPTURE = 2;

    static final int REQUEST_IMAGE_PICK = 100;
    private Uri takePhotoURI;
    private Uri selectedPhotoURI;
    static final int REQUEST_MULTI_PHOTO_PICK = 6;

    private static final int MAX_PHOTOS = 6;
    private ArrayList<Uri> photoUris = new ArrayList<>();
    private GridLayout photoGridLayout;
    private DatabaseReference recipesRef;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_recipe);

        recipeTitle = findViewById(R.id.recipeTitleText);
        recipeDescription = findViewById(R.id.recipeDescriptionText);
        cookingTime = findViewById(R.id.cookingTimeText);
        ingredients = findViewById(R.id.ingredientsText);
        directions = findViewById(R.id.directionsText);

        photoGridLayout = findViewById(R.id.photoGridLayout);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        addTagButton = findViewById(R.id.addTagButton);

        addPhotoButton = findViewById(R.id.addPhotoButton);
        postRecipeButton = findViewById(R.id.postRecipeButton);

        addPhotoOptions();
        presetTags();
        setAddTagButton();
        postRecipe();
        }


    private void addPhotoOptions(){
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUploadOptions();
            }
        });
    }

    private void imageUploadOptions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photos");
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                    else {
                        openCamera();
                    }
                }
                else if (which == 1){
                    openGallery();
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager())!= null){

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("CameraError", "Error occurred:", ex);

            }
            if (photoFile != null){
                takePhotoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMAGE_"+timeStamp;
        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName, ".jpg",  storageDirectory);
        return image;
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                photoUris.add(takePhotoURI);
                addPhotoToGridLayout(takePhotoURI);
            }
            else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        int numToAdd = Math.min(count, MAX_PHOTOS - photoUris.size());
                        for (int i = 0; i < numToAdd; i++) {
                            selectedPhotoURI = data.getClipData().getItemAt(i).getUri();
                            photoUris.add(selectedPhotoURI);
                            addPhotoToGridLayout(selectedPhotoURI);
                        }
                    }
                    else if (data.getData() != null) {
                        selectedPhotoURI = data.getData();
                        photoUris.add(selectedPhotoURI);
                        addPhotoToGridLayout(selectedPhotoURI);
                    }
                }
            }
        }
        if (photoUris.size() == MAX_PHOTOS) {
            addPhotoButton.setVisibility(View.GONE);
        }
    }

    private void addPhotoToGridLayout(Uri photoUri) {

        final View photoItemLayout = LayoutInflater.from(this).inflate(R.layout.item_photo, photoGridLayout, false);
        photoImageView = photoItemLayout.findViewById(R.id.photoImageView);
        deletePhotoButton = photoItemLayout.findViewById(R.id.deletePhotoButton);
        photoImageView.setImageURI(photoUri);

        deletePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoGridLayout.removeView(photoItemLayout);
                photoUris.remove(photoUri);

                if(photoUris.size()< MAX_PHOTOS && addPhotoButton.getVisibility() == View.GONE){
                    addPhotoButton.setVisibility(View.VISIBLE);
                }
            }
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 200;
        params.height = 200;
        params.rightMargin = 20;
        params.topMargin = 20;
        photoItemLayout.setLayoutParams(params);
        photoGridLayout.addView(photoItemLayout);
    }

    private void presetTags(){

        String[] presetTags = {"Food1", "Food2", "Food3"};

        for (String tag : presetTags){
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setBackgroundColor(R.color.purple);
            chip.setClickable(true);
            chip.setCheckable(true);
            chipGroupTags.addView(chip);
        }
    }

    private void setAddTagButton(){
        addTagButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadRecipeActivity.this);
                builder.setTitle("Add Custom Tag");

                final EditText input = new EditText(UploadRecipeActivity.this);
                builder.setView(input);

                builder.setPositiveButton("OK", ((dialog, which) -> {
                    String tagText = input.getText().toString();

                    if(!tagText.isEmpty()){
                        Chip chip = new Chip(UploadRecipeActivity.this);
                        chip.setText(tagText);
                        chip.setBackgroundColor(R.color.purple);
                        chip.setClickable(true);
                        chip.setCheckable(true);
                        chipGroupTags.addView(chip);
                    }
                }));

                builder.setNegativeButton("Cancel", ((dialog, which) -> dialog.cancel()));
                builder.show();
            }
        });
    }

    private void postRecipe(){
        postRecipeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                boolean isValid = true;

                String title = recipeTitle.getText().toString().trim();
                String description = recipeDescription.getText().toString().trim();
                String time = cookingTime.getText().toString().trim();
                String ingredientList = ingredients.getText().toString().trim();
                String directionList = directions.getText().toString().trim();

                String creator = UserSession.getUsername();

                List<String> photoUrisStrings = new ArrayList<>();
                for (Uri uri : photoUris){
                    photoUrisStrings.add(uri.toString());
                }

                List<String> tagStrings = new ArrayList<>();
                for (int i = 0; i < chipGroupTags.getChildCount(); i++){
                    Chip chip = (Chip) chipGroupTags.getChildAt(i);
                    if (chip.isChecked()){
                        tagStrings.add(chip.getText().toString());
                    }
                }

                if (title.isEmpty()) {
                    recipeTitle.setError("Title cannot be empty");
                    isValid = false;
                }
                if (description.isEmpty()) {
                    recipeDescription.setError("Description cannot be empty");
                    isValid = false;
                }
                if (time.isEmpty()) {
                    cookingTime.setError("Cooking time cannot be empty");
                    isValid = false;
                }
                if (ingredientList.isEmpty()) {
                    ingredients.setError("Ingredients cannot be empty");
                    isValid = false;
                }
                if (directionList.isEmpty()) {
                    directions.setError("Directions cannot be empty");
                    isValid = false;
                }

                if (!isValid) {
                    Toast.makeText(UploadRecipeActivity.this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadRecipeToFirebase(creator, title, description, photoUrisStrings, tagStrings);
            }
        });
    }

    private void uploadRecipeToFirebase(String creator, String title, String description, List<String> photoUris, List<String> tags) {
        recipesRef = FirebaseDatabase.getInstance().getReference().child("recipes");

        String recipeId = recipesRef.push().getKey();

        if (recipeId != null) {

            Recipe recipe = new Recipe(recipeId, title, creator, description, tags, photoUris);

            recipesRef.child(recipeId).setValue(recipe)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UploadRecipeActivity.this, "Recipe uploaded successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadRecipeActivity.this, DisplayRecipe.class);
                        intent.putExtra("recipeId", recipeId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UploadRecipeActivity.this, "Failed to upload recipe", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    public void logPhotoUris(List<Uri> uris){
        StringBuilder sb = new StringBuilder("Photo URIs: ");
        for (Uri uri : photoUris) {
            sb.append(uri.toString()).append("; ");
        }
        Log.d("PHOTOURI", sb.toString());
    }

}
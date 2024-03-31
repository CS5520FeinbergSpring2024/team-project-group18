package edu.northeastern.group18_finalproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.ImageView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadRecipeActivity extends AppCompatActivity {

    private ChipGroup chipGroupTags;
    private ImageButton addTagButton;

    private Button addPhotoButton, postRecipeButton;

    private ImageView photoImageView;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_IMAGE_PICK = 100;
    private Uri photoURI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_recipe);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        addTagButton = findViewById(R.id.addTagButton);

        addPhotoButton = findViewById(R.id.addPhotoButton);
        postRecipeButton = findViewById(R.id.postRecipeButton);

        photoImageView = findViewById(R.id.photoImageView);

        addPhotoOptions();
        presetTags();
        setAddTagButton();
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

    }



    public void openGallery(){
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_PICK){
            if ( data != null && data.getData() != null){
                photoURI = data.getData();

                Log.d("PHOTO URI", "onActivityResult: photo uri: " + photoURI.toString());
                photoImageView.setImageURI(photoURI);
                addPhotoButton.setEnabled(false);
            }
        }
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

}
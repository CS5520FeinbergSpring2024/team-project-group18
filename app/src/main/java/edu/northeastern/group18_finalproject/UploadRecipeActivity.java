package edu.northeastern.group18_finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class UploadRecipeActivity extends AppCompatActivity {

    private ChipGroup chipGroupTags;
    private ImageButton addTagButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_recipe);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        addTagButton = findViewById(R.id.addTagButton);
        presetTags();
        setAddTagButton();

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
                builder.setTitle("Add Tag");

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
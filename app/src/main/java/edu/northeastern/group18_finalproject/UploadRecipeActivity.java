package edu.northeastern.group18_finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
}
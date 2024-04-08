package edu.northeastern.group18_finalproject;

import java.util.List;

public class Recipe {
    private String recipeId;
    private String name;
    private String creator;
    private String description;
    private List<String> tags;
    private List<String> imageUrl;

    public Recipe() {
        // Default constructor required for Firebase
    }


    public Recipe(String recipeId, String title, String creator, String description,
                  List<String> tags, List<String> imageUrl) {
        this.recipeId = recipeId;
        this.name = title;
        this.creator = creator;
        this.description = description;
        this.tags = tags;
        this.imageUrl = imageUrl;
    }

    // Getters and setters

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }
}


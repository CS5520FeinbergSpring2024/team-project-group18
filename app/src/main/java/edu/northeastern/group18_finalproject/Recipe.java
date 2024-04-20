package edu.northeastern.group18_finalproject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe {
    private String recipeId;
    private String name;
    private String creator;
    private String description;
    private String cookingTime;
    private String ingredients;
    private String directions;

    private int likes;

    private Map<String, Boolean> likedBy = new HashMap<>();

    private List<String> tags;
    private List<String> imageUrl;

    public Recipe() {
        // Default constructor required for Firebase
    }


    public Recipe(String recipeId, String title, String creator, String description, String cookingTime, String ingredients, String directions,
                  List<String> tags, List<String> imageUrl) {
        this.recipeId = recipeId;
        this.name = title;
        this.creator = creator;
        this.description = description;
        this.cookingTime = cookingTime;
        this.ingredients = ingredients;
        this.directions = directions;
        this.tags = tags;
        this.imageUrl = imageUrl;
        this.likes = 0;
        this.likedBy = new HashMap<>();
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

    public String getCookingTime() {
        return cookingTime;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getDirections() {
        return directions;
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

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLikes() {
        return likes;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }
}


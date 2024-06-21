package com.example.haniph_n_finalproject;

public class Review {
    private String userEmail;
    private String reviewText;
    private int stars;

    public Review(String userEmail, String reviewText, int stars) {
        this.userEmail = userEmail;
        this.reviewText = reviewText;
        this.stars = stars;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getStars() {
        return stars;
    }
}

package com.example.haniph_n_finalproject;

public class Review {
    private String userEmail;
    private String reviewText;
    private int stars;
    private String movieId;
    private String userId;  // Add this field

    public Review(String userEmail, String reviewText, int stars, String movieId, String userId) {
        this.userEmail = userEmail;
        this.reviewText = reviewText;
        this.stars = stars;
        this.movieId = movieId;
        this.userId = userId;  // Initialize it in constructor
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

    public String getMovieId() {
        return movieId;
    }

    public String getUserId() {
        return userId;
    }
}


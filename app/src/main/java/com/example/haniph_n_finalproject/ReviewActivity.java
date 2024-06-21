package com.example.haniph_n_finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private TextInputEditText reviewEditText;
    private TextInputEditText starsEditText;
    private MaterialButton saveReviewButton;
    private MaterialButton deleteReviewButton;

    private String movieId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String reviewId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        reviewEditText = findViewById(R.id.review_edit_text);
        starsEditText = findViewById(R.id.stars_edit_text);
        saveReviewButton = findViewById(R.id.save_review_button);
        deleteReviewButton = findViewById(R.id.delete_review_button);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        movieId = getIntent().getStringExtra("movieId");

        // Check if the user has already written a review for this movie
        checkForExistingReview();

        // Add functionality to save the review
        saveReviewButton.setOnClickListener(v -> saveOrUpdateReview());

        // Add functionality to delete the review
        deleteReviewButton.setOnClickListener(v -> deleteReview());
    }

    private void checkForExistingReview() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QuerySnapshot snapshot = queryDocumentSnapshots;
                        reviewId = snapshot.getDocuments().get(0).getId();
                        String reviewText = snapshot.getDocuments().get(0).getString("reviewText");
                        int stars = snapshot.getDocuments().get(0).getLong("stars").intValue();

                        reviewEditText.setText(reviewText);
                        starsEditText.setText(String.valueOf(stars));

                        deleteReviewButton.setEnabled(true);
                    } else {
                        deleteReviewButton.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> Log.e("ReviewActivity", "Error checking for existing review", e));
    }

    private void saveOrUpdateReview() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = reviewEditText.getText().toString().trim();
        String starsText = starsEditText.getText().toString().trim();

        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Review cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int stars;
        try {
            stars = Integer.parseInt(starsText);
            if (stars < 1 || stars > 5) {
                Toast.makeText(this, "Stars must be between 1 and 5", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number of stars", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userEmail = currentUser.getEmail();

        Map<String, Object> review = new HashMap<>();
        review.put("movieId", movieId);
        review.put("userId", userId);
        review.put("userEmail", userEmail);
        review.put("reviewText", reviewText);
        review.put("stars", stars);

        if (reviewId != null) {
            db.collection("reviews").document(reviewId)
                    .set(review)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Review updated", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("reviews").add(review)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save review", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteReview() {
        if (currentUser == null || reviewId == null) {
            Toast.makeText(this, "No review to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("reviews").document(reviewId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                    reviewEditText.setText("");
                    starsEditText.setText("");

                    deleteReviewButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                });
    }
}


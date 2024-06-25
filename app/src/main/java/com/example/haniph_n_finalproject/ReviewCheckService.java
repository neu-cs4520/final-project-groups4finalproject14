package com.example.haniph_n_finalproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReviewCheckService extends Service {

    private FirebaseFirestore db;
    private ListenerRegistration reviewListener;
    private Set<String> favoritedMovieIds;
    private static final String TAG = "ReviewCheckService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is a started service, not a bound service
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        favoritedMovieIds = new HashSet<>();

        // Fetch the favorite movies once at the start
        fetchFavoriteMovies();

        // Start listening for new reviews
        startReviewListener();
    }

    private void fetchFavoriteMovies() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("favorites").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> movieIds = (List<String>) documentSnapshot.get("favoriteMovies");
                    if (movieIds != null) {
                        favoritedMovieIds.addAll(movieIds);
                    }
                });
    }

    private void startReviewListener() {
        reviewListener = db.collection("reviews")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        //Log.e(TAG, "Failed to load reviews", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        //Log.d(TAG, "Ratings query snapshot received");
                        handleNewReviews(queryDocumentSnapshots);
                    } else {
                        //Log.d(TAG, "Reviews query snapshot is null");
                    }



                });
    }

    private void handleNewReviews(QuerySnapshot snapshots) {
        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            if (dc.getType() == DocumentChange.Type.ADDED) {
                String movieId = dc.getDocument().getString("movieId");
                String userEmail = dc.getDocument().getString("userEmail");
                if (favoritedMovieIds.contains(movieId)) {
                    Intent intent = new Intent(this, ReviewBroadcastReceiver.class);
                    intent.putExtra("movieId", movieId);
                    intent.putExtra("reviewText", dc.getDocument().getString("reviewText"));
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("stars", dc.getDocument().getDouble("stars"));
                    //Log.d(TAG, "Sent Review for Movie: " + movieId + " from " + userEmail);
                    sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reviewListener != null) {
            reviewListener.remove();
        }
    }
}

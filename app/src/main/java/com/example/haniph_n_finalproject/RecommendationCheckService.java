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
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecommendationCheckService extends Service {

    private FirebaseFirestore db;
    private ListenerRegistration favoriteListener;
    private Set<String> favoritedMovieIds;
    private static final String TAG = "RecommendationCheckService";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";

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

        // Start listening for new favorite movies
        startFavoriteListener();
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

    private void startFavoriteListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoriteListener = db.collection("favorites").document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Failed to load favorites", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<String> favoriteMovieIds = (List<String>) snapshot.get("favoriteMovies");
                        if (favoriteMovieIds != null) {
                            for (String newMovieId : favoriteMovieIds) {
                                if (!favoritedMovieIds.contains(newMovieId)) {
                                    favoritedMovieIds.add(newMovieId);
                                    fetchRecommendationForMovie(newMovieId);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Favorites query snapshot is null");
                    }
                });
    }

    private void fetchRecommendationForMovie(String movieId) {
        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getRecommendedMovies(movieId, API_KEY).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> recommendations = response.body().getResults();
                    for (Movie recommendation : recommendations) {
                        if (!recommendation.getId().equals(movieId)) {
                            sendRecommendationBroadcast(recommendation.getTitle());
                            break; // Only take the first non-matching recommendation
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching recommendations", t);
            }
        });
    }

    private void sendRecommendationBroadcast(String recommendedMovieTitle) {
        Intent intent = new Intent(this, RecommendationBroadcastReceiver.class);
        intent.putExtra("recommendedMovieTitle", recommendedMovieTitle);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (favoriteListener != null) {
            favoriteListener.remove();
        }
    }
}

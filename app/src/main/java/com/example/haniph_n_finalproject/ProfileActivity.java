package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private RecyclerView favoritesRecyclerView;
    private RecyclerView recommendationsRecyclerView;
    private MovieAdapter favoriteMoviesAdapter;
    private MovieAdapter recommendationsAdapter;
    private List<Movie> favoriteMovies;
    private List<Movie> recommendedMovies;
    private Set<String> favoriteMovieIdsSet;
    private ListenerRegistration favoritesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    return true;
                } else if (id == R.id.navigation_search) {
                    startActivity(new Intent(ProfileActivity.this, MovieSearchActivity.class));
                    return true;
                } else if (id == R.id.navigation_profile) {
                    // Do nothing, we are already on the Profile screen
                    return true;
                }
                return false;
            }
        });

        // Highlight the current activity
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        // Display current user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            TextView welcomeMessage = findViewById(R.id.welcome_message);
            welcomeMessage.setText("Welcome, " + email + "!");
        }

        // Initialize RecyclerViews
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        favoriteMovies = new ArrayList<>();
        favoriteMovieIdsSet = new HashSet<>();
        favoriteMoviesAdapter = new MovieAdapter(favoriteMovies, this);
        favoritesRecyclerView.setAdapter(favoriteMoviesAdapter);

        recommendationsRecyclerView = findViewById(R.id.recommendations_recycler_view);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedMovies = new ArrayList<>();
        recommendationsAdapter = new MovieAdapter(recommendedMovies, this);
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);

        RecyclerView reviewedMoviesRecyclerView = findViewById(R.id.reviewed_movies_recycler_view);
        reviewedMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Fetch favorite movies in real-time
        fetchFavoriteMoviesInRealTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFavoriteMoviesInRealTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (favoritesListener != null) {
            favoritesListener.remove();
        }
    }

    private void fetchFavoriteMoviesInRealTime() {
        if (currentUser == null) return;

        if (favoritesListener != null) {
            favoritesListener.remove();
        }

        favoritesListener = db.collection("favorites").document(currentUser.getUid())
                .addSnapshotListener(MetadataChanges.INCLUDE, (documentSnapshot, e) -> {
                    if (e != null) {
                        Log.w("ProfileActivity", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> favoriteMovieIds = (List<String>) documentSnapshot.get("favoriteMovies");
                        updateFavoriteMovies(favoriteMovieIds);
                    } else {
                        Log.d("ProfileActivity", "Current data: null");
                        favoriteMovies.clear();
                        recommendedMovies.clear();
                        favoriteMovieIdsSet.clear();
                        favoriteMoviesAdapter.notifyDataSetChanged();
                        recommendationsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateFavoriteMovies(List<String> favoriteMovieIds) {
        // Clear the current list and set to avoid duplicates
        favoriteMovies.clear();
        recommendedMovies.clear();
        favoriteMovieIdsSet.clear();
        favoriteMoviesAdapter.notifyDataSetChanged();
        recommendationsAdapter.notifyDataSetChanged();

        if (favoriteMovieIds == null) return;

        for (String movieId : favoriteMovieIds) {
            fetchMovieDetails(movieId);
        }
    }

    private void fetchMovieDetails(String movieId) {
        if (favoriteMovieIdsSet.contains(movieId)) {
            return; // Skip if we already have this movie
        }

        favoriteMovieIdsSet.add(movieId);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getMovieDetails(movieId, API_KEY).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    if (!isMovieInFavorites(movie.getId())) {
                        favoriteMovies.add(movie);
                        favoriteMoviesAdapter.notifyDataSetChanged();
                        fetchRecommendationForMovie(movie);
                    }
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                // Handle error
            }
        });
    }

    private void fetchRecommendationForMovie(Movie favoriteMovie) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getRecommendedMovies(favoriteMovie.getId(), API_KEY).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> recommendations = response.body().getResults();
                    for (Movie recommendation : recommendations) {
                        if (!recommendation.getTitle().equals(favoriteMovie.getTitle())) {
                            recommendedMovies.add(recommendation);
                            recommendationsAdapter.notifyDataSetChanged();
                            break; // Only take the first non-matching recommendation
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                // Handle error
            }
        });
    }

    private boolean isMovieInFavorites(String movieId) {
        for (Movie movie : favoriteMovies) {
            if (movie.getId().equals(movieId)) {
                return true;
            }
        }
        return false;
    }
}


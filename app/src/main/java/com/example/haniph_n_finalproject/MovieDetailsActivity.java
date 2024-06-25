package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String TAG = "MovieDetailsActivity";

    private ImageView moviePoster;
    private TextView movieTitle;
    private TextView movieOverview;
    private TextView movieReleaseDate;
    private TextView movieGenres;
    private TextView movieVoteAverage;
    private RecyclerView castRecyclerView;
    private YouTubePlayerView youTubePlayerView;
    private RecyclerView reviewsRecyclerView;
    private MaterialButton writeReviewButton;
    private MaterialButton addToFavoritesButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String movieId;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Start the review background service
        Intent reviewServiceIntent = new Intent(this, ReviewCheckService.class);
        startService(reviewServiceIntent);

        // Start the recommendation background service
        Intent recommendationServiceIntent = new Intent(this, RecommendationCheckService.class);
        startService(recommendationServiceIntent);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        moviePoster = findViewById(R.id.movie_poster);
        movieTitle = findViewById(R.id.movie_title);
        movieOverview = findViewById(R.id.movie_overview);
        movieReleaseDate = findViewById(R.id.movie_release_date);
        movieGenres = findViewById(R.id.movie_genres);
        movieVoteAverage = findViewById(R.id.movie_vote_average);
        castRecyclerView = findViewById(R.id.cast_recycler_view);
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);
        writeReviewButton = findViewById(R.id.write_review_button);
        addToFavoritesButton = findViewById(R.id.add_to_favorites_button);

        // Get data from intent
        movieId = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        String overview = getIntent().getStringExtra("overview");
        String posterPath = getIntent().getStringExtra("posterPath");

        if (movieId == null) {
            Log.e(TAG, "Movie ID is null. Cannot fetch movie details.");
            return;
        }

        // Set data to views
        movieTitle.setText(title);
        movieOverview.setText(overview);
        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500" + posterPath)
                .into(moviePoster);

        // Fetch and display the movie details
        fetchMovieDetails(movieId);

        // Check if the movie is already in favorites
        checkIfFavorite(movieId);

        // Check if the user has already written a review for this movie
        checkForExistingReview();

        // Set up button to open ReviewActivity
        writeReviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(MovieDetailsActivity.this, ReviewActivity.class);
            intent.putExtra("movieId", movieId);
            startActivity(intent);
        });

        // Add functionality for "Add to Favorites" button
        addToFavoritesButton.setOnClickListener(v -> toggleFavorite(movieId));

        // Make sure to properly release YouTubePlayerView when the activity is destroyed
        getLifecycle().addObserver(youTubePlayerView);

        // Initialize reviews RecyclerView
        reviews = new ArrayList<>();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";
        reviewAdapter = new ReviewAdapter(reviews, this, currentUserId);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        // Fetch and display reviews
        fetchReviews(movieId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reviews.clear();
        fetchReviews(movieId);
        checkForExistingReview();
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
                        writeReviewButton.setText("Edit Review");
                    } else {
                        writeReviewButton.setText("Write a Review");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking for existing review", e));
    }

    private void fetchMovieDetails(String movieId) {
        //Log.d(TAG, "Fetching movie details for movieId: " + movieId);

        OkHttpClient client = new OkHttpClient.Builder().build();

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
                    //Log.d(TAG, "Movie details fetched successfully.");

                    movieReleaseDate.setText("Release Date: " + movie.getReleaseDate());
                    movieGenres.setText("Genres: " + getGenresString(movie.getGenres()));
                    movieVoteAverage.setText("Average TMDb Vote: " + String.format("%.1f", movie.getVoteAverage()) + "/10");

                    // Set up cast recycler view
                    fetchMovieCast(movieId);

                    // Fetch and display the movie trailer
                    fetchMovieTrailer(movieId);
                } else {
                    Log.e(TAG, "Failed to fetch movie details. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "Error fetching movie details", t);
            }
        });
    }

    private String getGenresString(List<Genre> genres) {
        StringBuilder genresString = new StringBuilder();
        for (Genre genre : genres) {
            if (genresString.length() > 0) {
                genresString.append(", ");
            }
            genresString.append(genre.getName());
        }
        return genresString.toString();
    }

    private void fetchMovieCast(String movieId) {
        //Log.d(TAG, "Fetching cast for movieId: " + movieId);

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getMovieCast(movieId, API_KEY).enqueue(new Callback<CastResponse>() {
            @Override
            public void onResponse(Call<CastResponse> call, Response<CastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cast> allCast = response.body().getCast().subList(0, 6);
                    List<Cast> actingCast = new ArrayList<>();

                    for (Cast cast : allCast) {
                        if ("Acting".equals(cast.getKnownForDepartment())) {
                            actingCast.add(cast);
                        }
                    }
                    //Log.d(TAG, "Cast fetched successfully: " + actingCast.size() + " acting cast members found.");
                    castRecyclerView.setLayoutManager(new LinearLayoutManager(MovieDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    castRecyclerView.setAdapter(new CastAdapter(actingCast));
                } else {
                    Log.e(TAG, "Failed to fetch cast. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<CastResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching cast", t);
            }
        });
    }

    private void fetchMovieTrailer(String movieId) {
        //Log.d(TAG, "Fetching movie trailer for movieId: " + movieId);

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getMovieTrailers(movieId, API_KEY).enqueue(new Callback<TrailersResponse>() {
            @Override
            public void onResponse(Call<TrailersResponse> call, Response<TrailersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Trailer> trailers = response.body().getResults();
                    //Log.d(TAG, "Trailers fetched successfully: " + trailers.size() + " trailers found.");
                    if (!trailers.isEmpty()) {
                        String videoKey = trailers.get(0).getKey();
                        //Log.d(TAG, "Video Key: " + videoKey);

                        // Initialize YouTube player with the fetched video key
                        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                            @Override
                            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                                youTubePlayer.cueVideo(videoKey, 0);
                            }
                        });
                    } else {
                        Log.d(TAG, "No trailers found for the movie.");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch trailers. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TrailersResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching movie trailers", t);
            }
        });
    }

    private void checkIfFavorite(String movieId) {
        if (currentUser == null) {
            Log.e(TAG, "No user logged in.");
            return;
        }

        DocumentReference docRef = db.collection("favorites").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> favoriteMovies = (List<String>) documentSnapshot.get("favoriteMovies");
                if (favoriteMovies != null && favoriteMovies.contains(movieId)) {
                    addToFavoritesButton.setText("Remove from Favorites");
                } else {
                    addToFavoritesButton.setText("Add to Favorites");
                }
            } else {
                addToFavoritesButton.setText("Add to Favorites");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching favorite movies", e));
    }

    private void toggleFavorite(String movieId) {
        if (currentUser == null) {
            Log.e(TAG, "No user logged in.");
            return;
        }

        DocumentReference docRef = db.collection("favorites").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> favoriteMovies;
            if (documentSnapshot.exists()) {
                favoriteMovies = (List<String>) documentSnapshot.get("favoriteMovies");
                if (favoriteMovies == null) {
                    favoriteMovies = new ArrayList<>();
                }

                if (favoriteMovies.contains(movieId)) {
                    favoriteMovies.remove(movieId);
                    Toast.makeText(MovieDetailsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    favoriteMovies.add(movieId);
                    Toast.makeText(MovieDetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
            } else {
                favoriteMovies = new ArrayList<>();
                favoriteMovies.add(movieId);
                Toast.makeText(MovieDetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("favoriteMovies", favoriteMovies);

            docRef.set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Favorite movies updated successfully."))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating favorite movies", e));

            checkIfFavorite(movieId);
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching favorite movies", e));
    }

    private void fetchReviews(String movieId) {
        db.collection("reviews")
                .whereEqualTo("movieId", movieId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Error fetching reviews", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            reviews.clear();
                            Review currentUserReview = null;
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                String userEmail = document.getString("userEmail");
                                String reviewText = document.getString("reviewText");
                                int stars = document.getLong("stars").intValue();
                                String userId = document.getString("userId");
                                Review review = new Review(userEmail, reviewText, stars, movieId, userId);

                                if (currentUser != null && currentUser.getUid().equals(userId)) {
                                    currentUserReview = review;
                                } else {
                                    reviews.add(review);
                                }
                            }
                            if (currentUserReview != null) {
                                reviews.add(0, currentUserReview);
                            }
                            reviewAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}



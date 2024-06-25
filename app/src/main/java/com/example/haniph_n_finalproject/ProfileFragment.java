package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private RecyclerView favoritesRecyclerView;
    private RecyclerView recommendationsRecyclerView;
    private RecyclerView reviewedMoviesRecyclerView;
    private MovieAdapter favoriteMoviesAdapter;
    private MovieAdapter recommendationsAdapter;
    private MovieAdapter reviewedMoviesAdapter;
    private List<Movie> favoriteMovies;
    private List<Movie> recommendedMovies;
    private List<Movie> reviewedMovies;
    private Set<String> favoriteMovieIdsSet;
    private Set<String> reviewedMovieIdsSet;
    private ListenerRegistration favoritesListener;
    private ListenerRegistration reviewsListener;
    private List<Genre> genres;
    private List<Genre> favoriteGenres;
    private RecyclerView favoriteGenresRecyclerView;
    private GenreChipAdapter favoriteGenresAdapter;
    private List<Genre> userFavoriteGenres;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Start the review background service
        Intent reviewServiceIntent = new Intent(getContext(), ReviewCheckService.class);
        getContext().startService(reviewServiceIntent);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        genres = new ArrayList<>();
        favoriteGenres = new ArrayList<>();

        // Display current user's email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            TextView welcomeMessage = view.findViewById(R.id.welcome_message);
            welcomeMessage.setText("Welcome, " + email + "!");
        }

        // Initialize RecyclerViews
        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        favoriteMovies = new ArrayList<>();
        favoriteMovieIdsSet = new HashSet<>();
        favoriteMoviesAdapter = new MovieAdapter(favoriteMovies, getContext());
        favoritesRecyclerView.setAdapter(favoriteMoviesAdapter);

        recommendationsRecyclerView = view.findViewById(R.id.recommendations_recycler_view);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedMovies = new ArrayList<>();
        recommendationsAdapter = new MovieAdapter(recommendedMovies, getContext());
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);

        reviewedMoviesRecyclerView = view.findViewById(R.id.reviewed_movies_recycler_view);
        reviewedMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        reviewedMovies = new ArrayList<>();
        reviewedMovieIdsSet = new HashSet<>();
        reviewedMoviesAdapter = new MovieAdapter(reviewedMovies, getContext());
        reviewedMoviesRecyclerView.setAdapter(reviewedMoviesAdapter);

        // Initialize favorite genres RecyclerView
        favoriteGenresRecyclerView = view.findViewById(R.id.favorite_genres_recycler_view);
        favoriteGenresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        userFavoriteGenres = new ArrayList<>();
        favoriteGenresAdapter = new GenreChipAdapter(userFavoriteGenres, getContext());
        favoriteGenresRecyclerView.setAdapter(favoriteGenresAdapter);

        // Fetch favorite movies in real-time
        fetchFavoriteMoviesInRealTime();

        // Fetch reviewed movies in real-time
        fetchReviewedMoviesInRealTime();

        // Setup Firestore listener for favorite genres
        setupFavoriteGenresListener();

        // Fetch favorite genres
        fetchFavoriteGenres();

        // Fetch genres for dropdown menu
        fetchGenres();

        // Add genre button click listener
        view.findViewById(R.id.add_genre_chip).setOnClickListener(v -> showAddGenreDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFavoriteMoviesInRealTime();
        fetchReviewedMoviesInRealTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (favoritesListener != null) {
            favoritesListener.remove();
        }
        if (reviewsListener != null) {
            reviewsListener.remove();
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
                        Log.w("ProfileFragment", "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> favoriteMovieIds = (List<String>) documentSnapshot.get("favoriteMovies");
                        updateFavoriteMovies(favoriteMovieIds);
                    } else {
                        Log.d("ProfileFragment", "Current data: null");
                        favoriteMovies.clear();
                        recommendedMovies.clear();
                        favoriteMovieIdsSet.clear();
                        favoriteMoviesAdapter.notifyDataSetChanged();
                        recommendationsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void fetchReviewedMoviesInRealTime() {
        if (currentUser == null) return;

        if (reviewsListener != null) {
            reviewsListener.remove();
        }

        reviewsListener = db.collection("reviews")
                .whereEqualTo("userId", currentUser.getUid())
                .addSnapshotListener(MetadataChanges.INCLUDE, (querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("ProfileFragment", "Listen failed.", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        Set<String> movieIds = new HashSet<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String movieId = document.getString("movieId");
                            if (movieId != null) {
                                movieIds.add(movieId);
                            }
                        }
                        updateReviewedMovies(new ArrayList<>(movieIds));
                    } else {
                        Log.d("ProfileFragment", "Current data: null");
                        reviewedMovies.clear();
                        reviewedMovieIdsSet.clear();
                        reviewedMoviesAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void updateFavoriteMovies(List<String> favoriteMovieIds) {
        // Clear the current list and set to avoid duplicates
        favoriteMovies.clear();
        recommendedMovies.clear();
        favoriteMovieIdsSet.clear();
        recommendationsAdapter.notifyDataSetChanged();

        if (favoriteMovieIds == null) return;

        for (String movieId : favoriteMovieIds) {
            fetchMovieDetails(movieId);
        }

    }

    private void updateReviewedMovies(List<String> reviewedMovieIds) {
        // Clear the current list and set to avoid duplicates
        reviewedMovies.clear();
        reviewedMovieIdsSet.clear();
        reviewedMoviesAdapter.notifyDataSetChanged();

        if (reviewedMovieIds == null) return;

        for (String movieId : reviewedMovieIds) {
            fetchReviewedMovieDetails(movieId);
        }
    }

    private void fetchMovieDetails(String movieId) {
        if (favoriteMovieIdsSet.contains(movieId)) {
            return; // Skip if we already have this movie
        }

        favoriteMovieIdsSet.add(movieId);

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

    private void fetchReviewedMovieDetails(String movieId) {
        if (reviewedMovieIdsSet.contains(movieId)) {
            return; // Skip if we already have this movie
        }

        reviewedMovieIdsSet.add(movieId);

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
                    if (!isMovieInReviewedMovies(movie.getId())) {
                        reviewedMovies.add(movie);
                        reviewedMoviesAdapter.notifyDataSetChanged();
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

        OkHttpClient client = new OkHttpClient.Builder().build();

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

    private boolean isMovieInReviewedMovies(String movieId) {
        for (Movie movie : reviewedMovies) {
            if (movie.getId().equals(movieId)) {
                return true;
            }
        }
        return false;
    }

    private void fetchGenres() {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getGenres(API_KEY).enqueue(new Callback<GenresResponse>() {
            @Override
            public void onResponse(Call<GenresResponse> call, Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    genres = response.body().getGenres();
                } else {
                    Snackbar.make(getView(), "Failed to fetch genres", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenresResponse> call, Throwable t) {
                Snackbar.make(getView(), "Error fetching genres", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showAddGenreDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_genre, null);
        MaterialAutoCompleteTextView genreDropdown = dialogView.findViewById(R.id.genre_dropdown);

        fetchGenresForDropdown(genreDropdown);

        builder.setView(dialogView)
                .setTitle("Add Genre")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Save", (dialog, which) -> {
                    String selectedGenreName = genreDropdown.getText().toString();
                    Genre selectedGenre = getGenreByName(selectedGenreName);
                    if (selectedGenre != null) {
                        saveFavoriteGenre(selectedGenre);
                    }
                })
                .show();
    }

    private void fetchGenresForDropdown(MaterialAutoCompleteTextView genreDropdown) {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getGenres(API_KEY).enqueue(new Callback<GenresResponse>() {
            @Override
            public void onResponse(Call<GenresResponse> call, Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Genre> genres = response.body().getGenres();
                    List<String> genreNames = new ArrayList<>();
                    for (Genre genre : genres) {
                        genreNames.add(genre.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, genreNames);
                    genreDropdown.setAdapter(adapter);
                } else {
                    Snackbar.make(getView(), "Failed to fetch genres", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenresResponse> call, Throwable t) {
                Snackbar.make(getView(), "Error fetching genres", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private Genre getGenreByName(String name) {
        for (Genre genre : genres) {
            if (genre.getName().equals(name)) {
                return genre;
            }
        }
        return null;
    }

    private void saveFavoriteGenre(Genre genre) {
        if (currentUser == null) return;

        DocumentReference userFavoriteGenresRef = db.collection("favoriteGenres").document(currentUser.getUid());

        userFavoriteGenresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Update existing document
                    List<Genre> favoriteGenres = new ArrayList<>();
                    List<Map<String, Object>> favoriteGenresMaps = (List<Map<String, Object>>) document.get("favoriteGenres");

                    if (favoriteGenresMaps != null) {
                        for (Map<String, Object> map : favoriteGenresMaps) {
                            int id = ((Long) map.get("id")).intValue();
                            String name = (String) map.get("name");
                            favoriteGenres.add(new Genre(id, name));
                        }
                    }

                    // Check if the genre already exists
                    boolean genreExists = false;
                    for (Genre existingGenre : favoriteGenres) {
                        if (existingGenre.getId() == genre.getId()) { // Use '==' to compare int values
                            genreExists = true;
                            break;
                        }
                    }

                    if (genreExists) {
                        Snackbar.make(getView(), genre.getName() + " is already in Favorite Genres", Snackbar.LENGTH_LONG).show();
                    } else {
                        favoriteGenres.add(genre);
                        userFavoriteGenresRef.update("favoriteGenres", favoriteGenres)
                                .addOnSuccessListener(aVoid -> Snackbar.make(getView(), genre.getName() + " was successfully added to Favorite Genres", Snackbar.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Snackbar.make(getView(), "Error adding genre", Snackbar.LENGTH_LONG).show());
                    }
                } else {
                    // Create new document
                    List<Genre> favoriteGenres = new ArrayList<>();
                    favoriteGenres.add(genre);
                    userFavoriteGenresRef.set(new FavoriteGenres(currentUser.getUid(), favoriteGenres))
                            .addOnSuccessListener(aVoid -> Snackbar.make(getView(), genre.getName() + " was successfully added to Favorite Genres", Snackbar.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Snackbar.make(getView(), "Error adding genre", Snackbar.LENGTH_LONG).show());
                }
            } else {
                Snackbar.make(getView(), "Error saving genre", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchFavoriteGenres() {
        if (currentUser == null) return;

        DocumentReference userFavoriteGenresRef = db.collection("favoriteGenres").document(currentUser.getUid());

        userFavoriteGenresRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Object>> favoriteGenresMaps = (List<Map<String, Object>>) document.get("favoriteGenres");

                    if (favoriteGenresMaps != null) {
                        userFavoriteGenres.clear();
                        for (Map<String, Object> map : favoriteGenresMaps) {
                            int id = ((Long) map.get("id")).intValue();
                            String name = (String) map.get("name");
                            userFavoriteGenres.add(new Genre(id, name));
                        }
                        favoriteGenresAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Snackbar.make(getView(), "Error fetching favorite genres", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupFavoriteGenresListener() {
        if (currentUser == null) return;

        db.collection("favoriteGenres").document(currentUser.getUid())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        List<Map<String, Object>> favoriteGenresList = (List<Map<String, Object>>) snapshot.get("favoriteGenres");
                        Log.d(TAG, "Favorite Genres List from Firestore: " + favoriteGenresList);

                        List<Genre> updatedUserGenresList = new ArrayList<>();
                        if (favoriteGenresList != null) {
                            for (Map<String, Object> genreMap : favoriteGenresList) {
                                int id = ((Long) genreMap.get("id")).intValue();
                                String name = (String) genreMap.get("name");
                                updatedUserGenresList.add(new Genre(id, name));
                            }
                        }

                        Log.d(TAG, "Updated User Genres List after processing: " + updatedUserGenresList);

                        // Update the RecyclerView adapter
                        favoriteGenresAdapter.updateGenres(updatedUserGenresList);
                    } else {
                        Log.d(TAG, "Current data: null");
                        favoriteGenresAdapter.updateGenres(new ArrayList<>());
                    }
                });
    }

}

package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private static final String TAG = "MovieSearchFragment";
    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private RecyclerView genresRecyclerView;
    private RecyclerView resultsRecyclerView;
    private GenreAdapter genreAdapter;
    private SearchResultsAdapter searchResultsAdapter;
    private List<Genre> genres;
    private List<Integer> selectedGenreIds;
    private List<Movie> searchResults;
    private EditText searchEditText;
    private Button searchButton;
    private Slider ratingSlider;
    private float selectedRating;
    private ImageView gearIcon;
    private LinearLayout filterContainer;
    private Button dateRangeButton;
    private long startDate;
    private long endDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        genresRecyclerView = view.findViewById(R.id.genres_recycler_view);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);
        ratingSlider = view.findViewById(R.id.rating_slider);
        gearIcon = view.findViewById(R.id.gear_icon);
        filterContainer = view.findViewById(R.id.filter_container);
        dateRangeButton = view.findViewById(R.id.date_range_button);

        TooltipCompat.setTooltipText(gearIcon, "Click to see advanced filters");

        // Initialize the RecyclerView for genres
        int numberOfColumns = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        genresRecyclerView.setLayoutManager(layoutManager);
        genres = new ArrayList<>();
        selectedGenreIds = new ArrayList<>();
        genreAdapter = new GenreAdapter(genres, selectedGenreIds);
        genresRecyclerView.setAdapter(genreAdapter);

        // Initialize the RecyclerView for search results
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter(searchResults, getContext());
        resultsRecyclerView.setAdapter(searchResultsAdapter);

        // Get the search query from the arguments
        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString("searchQuery");
            if (query != null && !query.isEmpty()) {
                searchEditText.setText(query);
                performSearch(query);
            }
        }

        searchButton.setOnClickListener(v -> {
            String newQuery = searchEditText.getText().toString().trim();
            selectedRating = ratingSlider.getValue();
            if (newQuery.isEmpty()) {
                Snackbar.make(searchButton, "Please enter a search query", Snackbar.LENGTH_SHORT).show();
            } else {
                performSearch(newQuery);
            }
        });

        gearIcon.setOnClickListener(v -> {
            if (filterContainer.getVisibility() == View.GONE) {
                filterContainer.setVisibility(View.VISIBLE);
            } else {
                filterContainer.setVisibility(View.GONE);
            }
        });

        // Fetch genres from TMDb API
        fetchGenres();

        dateRangeButton.setOnClickListener(v -> showDatePicker());

        return view;
    }

    private void fetchGenres() {
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

        tmDbApi.getGenres(API_KEY).enqueue(new Callback<GenresResponse>() {
            @Override
            public void onResponse(Call<GenresResponse> call, Response<GenresResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Genre> genres = response.body().getGenres();
                    displayGenres(genres);
                } else {
                    Snackbar.make(searchButton, "Failed to fetch genres", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GenresResponse> call, Throwable t) {
                Snackbar.make(searchButton, "Error fetching genres", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void displayGenres(List<Genre> genres) {
        this.genres.clear();
        this.genres.addAll(genres);
        genreAdapter.notifyDataSetChanged();
    }

    private void performSearch(String query) {
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

        tmDbApi.searchMovies(API_KEY, query).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> results = response.body().getResults();
                    displaySearchResults(results);
                } else {
                    Snackbar.make(searchButton, "Failed to fetch search results", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Snackbar.make(searchButton, "Error fetching search results", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void displaySearchResults(List<Movie> results) {
        searchResults.clear();
        for (Movie movie : results) {
            if (movie.getVoteAverage() <= selectedRating && movieMatchesGenres(movie) && movieMatchesReleaseDate(movie)) {
                Log.d(TAG, "Movie Result: " + movie.toString()); // Log movie details
                searchResults.add(movie);
            }
        }
        if (searchResults.isEmpty()) {
            Snackbar.make(searchButton, "No results found", Snackbar.LENGTH_LONG).show();
        }
        searchResultsAdapter.notifyDataSetChanged();
    }

    private boolean movieMatchesGenres(Movie movie) {
        if (selectedGenreIds.isEmpty()) {
            return true; // No genre filters selected, match all movies
        }
        if (movie.getGenreIds() == null || movie.getGenreIds().isEmpty()) {
            return false; // Movie has no genres, does not match
        }
        return movie.getGenreIds().containsAll(selectedGenreIds);
    }

    private boolean movieMatchesReleaseDate(Movie movie) {
        if (startDate == 0 && endDate == 0) {
            return true; // No date filter selected, match all movies
        }
        try {
            String releaseDate = movie.getReleaseDate();
            if (releaseDate == null || releaseDate.isEmpty()) {
                return false; // Invalid or empty date
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            long movieDate = sdf.parse(releaseDate).getTime();
            return (startDate == 0 || movieDate >= startDate) && (endDate == 0 || movieDate <= endDate);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing movie release date: " + movie.getReleaseDate(), e);
            return false;
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .build();

        datePicker.show(getChildFragmentManager(), "date_picker");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                startDate = selection.first;
                endDate = selection.second;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                dateRangeButton.setText(sdf.format(startDate) + " - " + sdf.format(endDate));
            }
        });

        datePicker.addOnNegativeButtonClickListener(dialog -> {
            startDate = 0;
            endDate = 0;
            dateRangeButton.setText("Select Date Range");
        });
    }
}

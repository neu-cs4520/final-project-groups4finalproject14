package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private RecyclerView trendingRecyclerView;
    private RecyclerView nowPlayingRecyclerView;
    private RecyclerView topRatedRecyclerView;
    private RecyclerView upcomingRecyclerView;
    private TrendingAdapter trendingAdapter;
    private TrendingAdapter nowPlayingAdapter;
    private TrendingAdapter topRatedAdapter;
    private TrendingAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        trendingRecyclerView = view.findViewById(R.id.trending_recycler_view);
        nowPlayingRecyclerView = view.findViewById(R.id.now_playing_recycler_view);
        topRatedRecyclerView = view.findViewById(R.id.top_rated_recycler_view);
        upcomingRecyclerView = view.findViewById(R.id.upcoming_recycler_view);

        // Initialize the RecyclerViews
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topRatedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Fetch popular movies
        fetchPopularMovies();

        // Fetch now playing movies
        fetchNowPlayingMovies();

        // Fetch top rated movies
        fetchTopRatedMovies();

        // Fetch upcoming movies
        fetchUpcomingMovies();

        return view;
    }

    private void fetchPopularMovies() {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getPopularMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies.size() > 6) {
                        movies = movies.subList(0, 6);
                    }
                    trendingAdapter = new TrendingAdapter(movies, getContext());
                    trendingRecyclerView.setAdapter(trendingAdapter);
                } else {
                    Snackbar.make(trendingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movies", t);
                Snackbar.make(trendingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchNowPlayingMovies() {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getNowPlayingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies.size() > 6) {
                        movies = movies.subList(0, 6);
                    }
                    nowPlayingAdapter = new TrendingAdapter(movies, getContext());
                    nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
                } else {
                    Snackbar.make(nowPlayingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movies", t);
                Snackbar.make(nowPlayingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchTopRatedMovies() {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getTopRatedMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies.size() > 6) {
                        movies = movies.subList(0, 6);
                    }
                    topRatedAdapter = new TrendingAdapter(movies, getContext());
                    topRatedRecyclerView.setAdapter(topRatedAdapter);
                } else {
                    Snackbar.make(topRatedRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movies", t);
                Snackbar.make(topRatedRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchUpcomingMovies() {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getUpcomingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    if (movies.size() > 6) {
                        movies = movies.subList(0, 6);
                    }
                    upcomingAdapter = new TrendingAdapter(movies, getContext());
                    upcomingRecyclerView.setAdapter(upcomingAdapter);
                } else {
                    Snackbar.make(upcomingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error fetching movies", t);
                Snackbar.make(upcomingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}

package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        trendingRecyclerView = findViewById(R.id.trending_recycler_view);
        nowPlayingRecyclerView = findViewById(R.id.now_playing_recycler_view);
        topRatedRecyclerView = findViewById(R.id.top_rated_recycler_view);
        upcomingRecyclerView = findViewById(R.id.upcoming_recycler_view);

        // Initialize the RecyclerViews
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topRatedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    // Do nothing, we are already on the Home screen
                    return true;
                } else if (id == R.id.navigation_search) {
                    startActivity(new Intent(HomeActivity.this, MovieSearchActivity.class));
                    return true;
                } else if (id == R.id.navigation_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Highlight the current activity
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Fetch popular movies
        fetchPopularMovies();

        // Fetch now playing movies
        fetchNowPlayingMovies();

        // Fetch top rated movies
        fetchTopRatedMovies();

        // Fetch upcoming movies
        fetchUpcomingMovies();
    }

    private void fetchPopularMovies() {
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

        tmDbApi.getPopularMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    trendingAdapter = new TrendingAdapter(movies, HomeActivity.this);
                    trendingRecyclerView.setAdapter(trendingAdapter);
                } else {
                    Snackbar.make(trendingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching movies", t);
                Snackbar.make(trendingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchNowPlayingMovies() {
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

        tmDbApi.getNowPlayingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    nowPlayingAdapter = new TrendingAdapter(movies, HomeActivity.this);
                    nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
                } else {
                    Snackbar.make(nowPlayingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching movies", t);
                Snackbar.make(nowPlayingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchTopRatedMovies() {
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

        tmDbApi.getTopRatedMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    topRatedAdapter = new TrendingAdapter(movies, HomeActivity.this);
                    topRatedRecyclerView.setAdapter(topRatedAdapter);
                } else {
                    Snackbar.make(topRatedRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching movies", t);
                Snackbar.make(topRatedRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void fetchUpcomingMovies() {
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

        tmDbApi.getUpcomingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    upcomingAdapter = new TrendingAdapter(movies, HomeActivity.this);
                    upcomingRecyclerView.setAdapter(upcomingAdapter);
                } else {
                    Snackbar.make(upcomingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                Log.e("HomeActivity", "Error fetching movies", t);
                Snackbar.make(upcomingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}


//package com.example.haniph_n_finalproject;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import com.google.android.material.snackbar.Snackbar;
//
//import java.util.List;
//
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class HomeActivity extends AppCompatActivity {
//
//    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
//    private static final String BASE_URL = "https://api.themoviedb.org/3/";
//
//    private RecyclerView trendingRecyclerView;
//    private RecyclerView nowPlayingRecyclerView;
//    private RecyclerView topRatedRecyclerView;
//    private RecyclerView upcomingRecyclerView;
//    private TrendingAdapter trendingAdapter;
//    private TrendingAdapter nowPlayingAdapter;
//    private TrendingAdapter topRatedAdapter;
//    private TrendingAdapter upcomingAdapter;
//    private EditText searchEditText;
//    private Button searchButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home);
//
//        searchEditText = findViewById(R.id.search_edit_text);
//        searchButton = findViewById(R.id.search_button);
//        trendingRecyclerView = findViewById(R.id.trending_recycler_view);
//        nowPlayingRecyclerView = findViewById(R.id.now_playing_recycler_view);
//        topRatedRecyclerView = findViewById(R.id.top_rated_recycler_view);
//        upcomingRecyclerView = findViewById(R.id.upcoming_recycler_view);
//
//        // Initialize the RecyclerViews
//        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        nowPlayingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        topRatedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        upcomingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String query = searchEditText.getText().toString().trim();
//                if (query.isEmpty()) {
//                    Snackbar.make(searchButton, "Please enter a search query", Snackbar.LENGTH_SHORT).show();
//                } else {
//                    Intent intent = new Intent(HomeActivity.this, MovieSearchActivity.class);
//                    intent.putExtra("searchQuery", query);
//                    startActivity(intent);
//                }
//            }
//        });
//
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                int id = item.getItemId();
//                if (id == R.id.navigation_home) {
//                    // Do nothing, we are already on the Home screen
//                    return true;
//                } else if (id == R.id.navigation_search) {
//                    startActivity(new Intent(HomeActivity.this, MovieSearchActivity.class));
//                    return true;
//                } else if (id == R.id.navigation_profile) {
//                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        // Highlight the current activity
//        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
//
//        // Fetch popular movies
//        fetchPopularMovies();
//
//        // Fetch now playing movies
//        fetchNowPlayingMovies();
//
//        // Fetch top rated movies
//        fetchTopRatedMovies();
//
//        // Fetch upcoming movies
//        fetchUpcomingMovies();
//    }
//
//    private void fetchPopularMovies() {
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);
//
//        tmDbApi.getPopularMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
//            @Override
//            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Movie> movies = response.body().getResults();
//                    trendingAdapter = new TrendingAdapter(movies, HomeActivity.this);
//                    trendingRecyclerView.setAdapter(trendingAdapter);
//                } else {
//                    Snackbar.make(trendingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MoviesResponse> call, Throwable t) {
//                Log.e("HomeActivity", "Error fetching movies", t);
//                Snackbar.make(trendingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void fetchNowPlayingMovies() {
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);
//
//        tmDbApi.getNowPlayingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
//            @Override
//            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Movie> movies = response.body().getResults();
//                    nowPlayingAdapter = new TrendingAdapter(movies, HomeActivity.this);
//                    nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
//                } else {
//                    Snackbar.make(nowPlayingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MoviesResponse> call, Throwable t) {
//                Log.e("HomeActivity", "Error fetching movies", t);
//                Snackbar.make(nowPlayingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void fetchTopRatedMovies() {
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);
//
//        tmDbApi.getTopRatedMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
//            @Override
//            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Movie> movies = response.body().getResults();
//                    topRatedAdapter = new TrendingAdapter(movies, HomeActivity.this);
//                    topRatedRecyclerView.setAdapter(topRatedAdapter);
//                } else {
//                    Snackbar.make(topRatedRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MoviesResponse> call, Throwable t) {
//                Log.e("HomeActivity", "Error fetching movies", t);
//                Snackbar.make(topRatedRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    private void fetchUpcomingMovies() {
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);
//
//        tmDbApi.getUpcomingMovies(API_KEY, 1).enqueue(new Callback<MoviesResponse>() {
//            @Override
//            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Movie> movies = response.body().getResults();
//                    upcomingAdapter = new TrendingAdapter(movies, HomeActivity.this);
//                    upcomingRecyclerView.setAdapter(upcomingAdapter);
//                } else {
//                    Snackbar.make(upcomingRecyclerView, "Failed to fetch data", Snackbar.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<MoviesResponse> call, Throwable t) {
//                Log.e("HomeActivity", "Error fetching movies", t);
//                Snackbar.make(upcomingRecyclerView, "Error fetching data", Snackbar.LENGTH_LONG).show();
//            }
//        });
//    }
//}
//

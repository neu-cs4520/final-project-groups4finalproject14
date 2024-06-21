package com.example.haniph_n_finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class MovieSearchActivity extends AppCompatActivity {

    private RecyclerView trendingRecyclerView;
    private TrendingAdapter trendingAdapter;
    private List<String> trendingMovies;
    private EditText searchEditText;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        trendingRecyclerView = findViewById(R.id.trending_recycler_view);

        // Initialize the RecyclerView
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trendingMovies = new ArrayList<>();
        //trendingAdapter = new TrendingAdapter(trendingMovies);
        trendingRecyclerView.setAdapter(trendingAdapter);

        // Get the search query from the intent
        Intent intent = getIntent();
        String query = intent.getStringExtra("searchQuery");
        if (query != null && !query.isEmpty()) {
            searchEditText.setText(query);
            // Perform the search (to be implemented)
            Snackbar.make(searchButton, "Searching for: " + query, Snackbar.LENGTH_SHORT).show();
        }

        searchButton.setOnClickListener(v -> {
            String newQuery = searchEditText.getText().toString().trim();
            if (newQuery.isEmpty()) {
                Snackbar.make(searchButton, "Please enter a search query", Snackbar.LENGTH_SHORT).show();
            } else {
                // Perform the search (to be implemented)
                Snackbar.make(searchButton, "Searching for: " + newQuery, Snackbar.LENGTH_SHORT).show();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    startActivity(new Intent(MovieSearchActivity.this, HomeActivity.class));
                    return true;
                } else if (id == R.id.navigation_search) {
                    // Do nothing, we are already on the Search screen
                    return true;
                } else if (id == R.id.navigation_profile) {
                    startActivity(new Intent(MovieSearchActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Highlight the current activity
        bottomNavigationView.setSelectedItemId(R.id.navigation_search);
    }
}




//package com.example.haniph_n_finalproject;
//
//import android.os.Bundle;
//import android.content.Intent;
//import android.widget.EditText;
//import android.widget.Button;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.android.material.snackbar.Snackbar;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MovieSearchActivity extends AppCompatActivity {
//
//    private RecyclerView trendingRecyclerView;
//    private TrendingAdapter trendingAdapter;
//    private List<String> trendingMovies;
//    private EditText searchEditText;
//    private Button searchButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_movie_search);
//
//        searchEditText = findViewById(R.id.search_edit_text);
//        searchButton = findViewById(R.id.search_button);
//        trendingRecyclerView = findViewById(R.id.trending_recycler_view);
//
//        // Initialize the RecyclerView
//        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        trendingMovies = new ArrayList<>();
//        trendingAdapter = new TrendingAdapter(trendingMovies);
//        trendingRecyclerView.setAdapter(trendingAdapter);
//
//        // Get the search query from the intent
//        Intent intent = getIntent();
//        String query = intent.getStringExtra("searchQuery");
//        if (query != null && !query.isEmpty()) {
//            searchEditText.setText(query);
//            // Perform the search (to be implemented)
//            Snackbar.make(searchButton, "Searching for: " + query, Snackbar.LENGTH_SHORT).show();
//        }
//
//        searchButton.setOnClickListener(v -> {
//            String newQuery = searchEditText.getText().toString().trim();
//            if (newQuery.isEmpty()) {
//                Snackbar.make(searchButton, "Please enter a search query", Snackbar.LENGTH_SHORT).show();
//            } else {
//                // Perform the search (to be implemented)
//                Snackbar.make(searchButton, "Searching for: " + newQuery, Snackbar.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
//

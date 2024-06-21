package com.example.haniph_n_finalproject;

import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String poster_path;
    private String overview;
    private String release_date;
    private List<Genre> genres;
    private float vote_average;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public List<Genre> getGenres() {
        return genres;
    }


    public float getVoteAverage() {
        return vote_average;
    }
}

class Genre {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}



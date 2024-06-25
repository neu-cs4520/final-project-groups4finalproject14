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
    private List<Integer> genre_ids;

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

    public List<Integer> getGenreIds() {
        return genre_ids;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genre_ids = genreIds;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", poster_path='" + poster_path + '\'' +
                ", overview='" + overview + '\'' +
                ", release_date='" + release_date + '\'' +
                ", genres=" + genres +
                ", vote_average=" + vote_average +
                ", genre_ids=" + genre_ids +
                '}';
    }
}


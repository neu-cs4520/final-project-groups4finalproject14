package com.example.haniph_n_finalproject;

import java.util.List;

public class FavoriteGenres {
    private String userId;
    private List<Genre> favoriteGenres;

    public FavoriteGenres(String userId, List<Genre> favoriteGenres) {
        this.userId = userId;
        this.favoriteGenres = favoriteGenres;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Genre> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<Genre> favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }
}


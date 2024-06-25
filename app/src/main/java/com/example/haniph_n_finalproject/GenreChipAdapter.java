package com.example.haniph_n_finalproject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreChipAdapter extends RecyclerView.Adapter<GenreChipAdapter.GenreChipViewHolder> {

    private List<Genre> genres;
    private Context context;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    public GenreChipAdapter(List<Genre> genres, Context context) {
        this.genres = genres;
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.currentUser = auth.getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public GenreChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre_chip, parent, false);
        return new GenreChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreChipViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.genreChip.setText(genre.getName());
        holder.genreChip.setCloseIconVisible(true);

        holder.genreChip.setOnCloseIconClickListener(v -> {
            // Remove the genre locally
            genres.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, genres.size());

            // Update Firestore database to remove the genre from the user's favorite genres
            removeFavoriteGenreFromFirestore(genre);
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    public void updateGenres(List<Genre> newGenres) {
        genres.clear();
        genres.addAll(newGenres);
        notifyDataSetChanged();
    }


    private void removeFavoriteGenreFromFirestore(Genre genre) {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DocumentReference docRef = db.collection("favoriteGenres").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> favoriteGenresList = (List<Map<String, Object>>) documentSnapshot.get("favoriteGenres");
                List<Genre> favoriteGenres = new ArrayList<>();
                for (Map<String, Object> genreMap : favoriteGenresList) {
                    int id = ((Long) genreMap.get("id")).intValue();
                    String name = (String) genreMap.get("name");
                    favoriteGenres.add(new Genre(id, name));
                }

                favoriteGenres.removeIf(existingGenre -> existingGenre.getId() == genre.getId());

                List<Map<String, Object>> updatedFavoriteGenresList = new ArrayList<>();
                for (Genre favoriteGenre : favoriteGenres) {
                    Map<String, Object> genreMap = new HashMap<>();
                    genreMap.put("id", favoriteGenre.getId());
                    genreMap.put("name", favoriteGenre.getName());
                    updatedFavoriteGenresList.add(genreMap);
                }

                docRef.update("favoriteGenres", updatedFavoriteGenresList)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("GenreChipAdapter", "Favorite genre successfully removed!");
                            Snackbar.make(((Activity) context).findViewById(android.R.id.content), genre.getName() + " was successfully removed from Favorite Genres", Snackbar.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.w("GenreChipAdapter", "Error removing favorite genre", e);
                            Snackbar.make(((Activity) context).findViewById(android.R.id.content), "Failed to remove genre", Snackbar.LENGTH_LONG).show();
                        });
            }
        }).addOnFailureListener(e -> Log.w("GenreChipAdapter", "Error getting favorite genres", e));
    }

    public static class GenreChipViewHolder extends RecyclerView.ViewHolder {
        Chip genreChip;

        public GenreChipViewHolder(@NonNull View itemView) {
            super(itemView);
            genreChip = itemView.findViewById(R.id.genre_chip);
        }
    }
}


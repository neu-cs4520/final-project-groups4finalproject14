package com.example.haniph_n_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    private List<Genre> genres;
    private List<Integer> selectedGenreIds;

    public GenreAdapter(List<Genre> genres, List<Integer> selectedGenreIds) {
        this.genres = genres;
        this.selectedGenreIds = selectedGenreIds;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.checkBox.setText(genre.getName());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedGenreIds.contains(genre.getId()));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedGenreIds.add(genre.getId());
            } else {
                selectedGenreIds.remove(Integer.valueOf(genre.getId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.genre_checkbox);
        }
    }
}

package com.example.haniph_n_finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder> {

    private List<Movie> trendingMovies;
    private Context context;

    public TrendingAdapter(List<Movie> trendingMovies, Context context) {
        this.trendingMovies = trendingMovies;
        this.context = context;
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trending_movie, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        Movie movie = trendingMovies.get(position);
        holder.movieTitle.setText(movie.getTitle());
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                .into(holder.moviePoster);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra("id", movie.getId());
                intent.putExtra("title", movie.getTitle());
                intent.putExtra("overview", movie.getOverview());
                intent.putExtra("posterPath", movie.getPosterPath());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trendingMovies.size();
    }

    public static class TrendingViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle;
        ImageView moviePoster;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movie_title);
            moviePoster = itemView.findViewById(R.id.movie_poster);
        }
    }
}



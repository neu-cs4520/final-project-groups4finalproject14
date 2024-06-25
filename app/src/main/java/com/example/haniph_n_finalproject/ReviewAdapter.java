package com.example.haniph_n_finalproject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private Context context;
    private String currentUserId;
    private static final String API_KEY = "f395e60703b619ebfdb8421e6a5d94bd";
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    public ReviewAdapter(List<Review> reviews, Context context, String currentUserId) {
        this.reviews = reviews;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.userEmail.setText(review.getUserEmail());
        holder.reviewText.setText(review.getReviewText());
        holder.stars.setText(review.getStars() + "/5");

        if (review.getUserId().equals(currentUserId)) {
            holder.overflowMenu.setVisibility(View.VISIBLE);
            holder.overflowMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(context, holder.overflowMenu);
                popupMenu.inflate(R.menu.review_item_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    fetchMovieName(review.getMovieId(), movieName -> {
                        if (item.getItemId() == R.id.menu_share_email) {
                            shareViaEmail(review, movieName);
                        } else if (item.getItemId() == R.id.menu_share_twitter) {
                            shareViaTwitter(review, movieName);
                        } else if (item.getItemId() == R.id.menu_share_reddit) {
                            shareViaReddit(review, movieName);
                        }
                    });
                    return true;
                });
                popupMenu.show();
            });
        } else {
            holder.overflowMenu.setVisibility(View.GONE);
        }
    }

    private void fetchMovieName(String movieId, MovieNameCallback callback) {

        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TMDbApi tmDbApi = retrofit.create(TMDbApi.class);

        tmDbApi.getMovieDetails(movieId, API_KEY).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onMovieNameFetched(response.body().getTitle());
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                callback.onMovieNameFetched("Unknown Movie");
            }
        });
    }

    private void shareViaEmail(Review review, String movieName) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out my movie review!");
        intent.putExtra(Intent.EXTRA_TEXT, "Review: " + review.getReviewText() + "\nRating: " + review.getStars() + "/5\nMovie: " + movieName);
        context.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    private void shareViaTwitter(Review review, String movieName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String tweetUrl = "https://twitter.com/intent/tweet?text=" + "Review: " + review.getReviewText() + " Rating: " + review.getStars() + "/5 Movie: " + movieName;
        intent.setData(Uri.parse(tweetUrl));
        context.startActivity(intent);
    }

    private void shareViaReddit(Review review, String movieName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String redditUrl = "https://www.reddit.com/r/moviereviews/submit?title=" + Uri.encode("My Review for " + movieName + " (" + review.getStars() + " stars)");
        intent.setData(Uri.parse(redditUrl));
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail;
        TextView reviewText;
        TextView stars;
        ImageView overflowMenu;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.review_user_email);
            reviewText = itemView.findViewById(R.id.review_text);
            stars = itemView.findViewById(R.id.review_stars);
            overflowMenu = itemView.findViewById(R.id.overflow_menu);
        }
    }

    private interface MovieNameCallback {
        void onMovieNameFetched(String movieName);
    }
}


//package com.example.haniph_n_finalproject;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.bumptech.glide.Glide;
//
//import java.util.List;
//
//public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
//
//    private List<Review> reviews;
//    private Context context;
//    private String currentUserId;  // Add a field for current user ID
//
//    public ReviewAdapter(List<Review> reviews, Context context, String currentUserId) {
//        this.reviews = reviews;
//        this.context = context;
//        this.currentUserId = currentUserId;  // Initialize it in constructor
//    }
//
//    @NonNull
//    @Override
//    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
//        return new ReviewViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
//        Review review = reviews.get(position);
//        holder.userEmail.setText(review.getUserEmail());
//        holder.reviewText.setText(review.getReviewText());
//        holder.stars.setText(review.getStars() + "/5");
//
//        // Check if the review is written by the current user
//        if (review.getUserId().equals(currentUserId)) {
//            holder.overflowMenu.setVisibility(View.VISIBLE);
//            holder.overflowMenu.setOnClickListener(v -> {
//                PopupMenu popupMenu = new PopupMenu(context, holder.overflowMenu);
//                popupMenu.inflate(R.menu.review_item_menu);
//                popupMenu.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.menu_share_email) {
//                        shareViaEmail(review);
//                        return true;
//                    } else if (item.getItemId() == R.id.menu_share_twitter) {
//                        shareViaTwitter(review);
//                        return true;
//                    } else if (item.getItemId() == R.id.menu_share_reddit) {
//                        shareViaReddit(review);
//                        return true;
//                    }
//                    return false;
//                });
//                popupMenu.show();
//            });
//        } else {
//            holder.overflowMenu.setVisibility(View.GONE);  // Hide the overflow menu icon for other users
//        }
//    }
//
//    private void shareViaEmail(Review review) {
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("message/rfc822");
//        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out my movie review!");
//        intent.putExtra(Intent.EXTRA_TEXT, "Review: " + review.getReviewText() + "\nRating: " + review.getStars() + "/5\nMovie: " + review.getMovieId());
//        context.startActivity(Intent.createChooser(intent, "Send Email"));
//    }
//
//    private void shareViaTwitter(Review review) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String tweetUrl = "https://twitter.com/intent/tweet?text=" + "Review: " + review.getReviewText() + " Rating: " + review.getStars() + "/5 Movie: " + review.getMovieId();
//        intent.setData(Uri.parse(tweetUrl));
//        context.startActivity(intent);
//    }
//
//    private void shareViaReddit(Review review) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String redditUrl = "https://www.reddit.com/r/moviereviews/submit?title=" + Uri.encode("Movie Review") + "&text=" + Uri.encode("Review: " + review.getReviewText() + " Rating: " + review.getStars() + "/5 Movie: " + review.getMovieId());
//        intent.setData(Uri.parse(redditUrl));
//        context.startActivity(intent);
//    }
//
//    @Override
//    public int getItemCount() {
//        return reviews.size();
//    }
//
//    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
//        TextView userEmail;
//        TextView reviewText;
//        TextView stars;
//        ImageView overflowMenu;
//
//        public ReviewViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userEmail = itemView.findViewById(R.id.review_user_email);
//            reviewText = itemView.findViewById(R.id.review_text);
//            stars = itemView.findViewById(R.id.review_stars);
//            overflowMenu = itemView.findViewById(R.id.overflow_menu);
//        }
//    }
//}
//

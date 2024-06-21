package com.example.haniph_n_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.haniph_n_finalproject.R;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.userEmail.setText(review.getUserEmail());
        holder.reviewRating.setText(review.getStars() + "/5");
        holder.reviewText.setText(review.getReviewText());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userEmail;
        TextView reviewRating;
        TextView reviewText;
        ImageView ratingStar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.user_email);
            reviewRating = itemView.findViewById(R.id.review_rating);
            reviewText = itemView.findViewById(R.id.review_text);
            ratingStar = itemView.findViewById(R.id.star_icon);
        }
    }
}

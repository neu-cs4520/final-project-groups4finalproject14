package com.example.haniph_n_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private List<Cast> castList;

    public CastAdapter(List<Cast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.castName.setText(cast.getName());
        holder.castCharacter.setText(cast.getCharacter());
        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w500" + cast.getProfilePath())
                .into(holder.castImage);
    }

    @Override
    public int getItemCount() {
        return castList.size();
    }

    public static class CastViewHolder extends RecyclerView.ViewHolder {
        TextView castName;
        TextView castCharacter;
        ImageView castImage;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            castName = itemView.findViewById(R.id.cast_name);
            castCharacter = itemView.findViewById(R.id.cast_character);
            castImage = itemView.findViewById(R.id.cast_image);
        }
    }
}

package com.fizzycoyote.qusetroll;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class DiceAdapter extends RecyclerView.Adapter<DiceAdapter.DiceViewHolder> {
    private List<Dice> diceList;

    public DiceAdapter(List<Dice> diceList) {
        this.diceList = diceList;
    }

    static class DiceViewHolder extends RecyclerView.ViewHolder {
        ImageView diceGifImageView;

        public DiceViewHolder(@NonNull View itemView) {
            super(itemView);
            diceGifImageView = itemView.findViewById(R.id.diceGifImageView); // Upewnij się, że ID się zgadza
        }
    }

    @NonNull
    @Override
    public DiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dice, parent, false);
        return new DiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiceViewHolder holder, int position) {
        Dice dice = diceList.get(position);
        int gifResource = dice.getGifResource(holder.itemView.getContext(), dice.getResult()); // Użyj wyniku rzutu

        Log.d("DiceAdapter", "Binding dice: " + dice.getType() + ", result: " + dice.getResult() + ", GIF resource: " + gifResource);

        if (holder.diceGifImageView != null) {
            if (dice.isRolled()) { // If the dice has been rolled
                Glide.with(holder.itemView.getContext())
                        .asGif()
                        .load(gifResource)
                        .listener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                Log.e("DiceAdapter", "Failed to load GIF: " + (e != null ? e.getMessage() : "Unknown error"));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("DiceAdapter", "GIF loaded successfully: " + dice.getType() + ", result: " + dice.getResult());
                                resource.setLoopCount(1);
                                return false;
                            }
                        })
                        .into(holder.diceGifImageView);
            } else {
                Glide.with(holder.itemView.getContext())
                        .asBitmap()
                        .load(gifResource)
                        .into(holder.diceGifImageView);
            }
        } else {
            Log.e("DiceAdapter", "diceGifImageView is null");
            Glide.with(holder.itemView.getContext()).clear(holder.diceGifImageView);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDiceList(List<Dice> updatedList) {
        this.diceList = updatedList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return diceList.size();
    }

    public Dice getItem(int position) {
        return diceList.get(position);
    }
}
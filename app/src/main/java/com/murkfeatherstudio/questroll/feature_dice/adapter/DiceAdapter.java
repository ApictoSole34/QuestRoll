package com.murkfeatherstudio.questroll.feature_dice.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.murkfeatherstudio.questroll.databinding.ItemDiceBinding;
import com.murkfeatherstudio.questroll.feature_dice.model.Dice;

import java.util.List;

public class DiceAdapter extends RecyclerView.Adapter<DiceAdapter.DiceViewHolder> {
    private List<Dice> diceList;

    public DiceAdapter(List<Dice> diceList) {
        this.diceList = diceList;
    }

    static class DiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemDiceBinding binding;

        public DiceViewHolder(@NonNull ItemDiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public DiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDiceBinding binding = ItemDiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DiceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiceViewHolder holder, int position) {
        Dice dice = diceList.get(position);
        int gifResource = dice.getGifResource(holder.itemView.getContext(), dice.getResult());

        Log.d("DiceAdapter", "Binding dice: " + dice.getType() + ", result: " + dice.getResult() + ", GIF resource: " + gifResource);

        if (dice.isRolled() && !dice.isAnimationPlayed()) {
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
                            dice.setAnimationPlayed(true);
                            return false;
                        }
                    })
                    .into(holder.binding.diceGifImageView);
        } else {
            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(gifResource)
                    .into(holder.binding.diceGifImageView);
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
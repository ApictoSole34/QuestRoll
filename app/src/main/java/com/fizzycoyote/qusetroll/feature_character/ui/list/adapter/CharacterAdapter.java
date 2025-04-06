package com.fizzycoyote.qusetroll.feature_character.ui.list.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterRPG;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {
    private List<CharacterRPG> characterRPGList;
    private OnCharacterClickListener listener;

    public CharacterAdapter(List<CharacterRPG> characterRPGList, OnCharacterClickListener listener) {
        this.characterRPGList = characterRPGList;
        this.listener = listener;
    }

    public void setCharacterList(List<CharacterRPG> characterRPGList) {
        this.characterRPGList = characterRPGList;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character,parent,false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        if (position < characterRPGList.size()) {
            CharacterRPG characterRPG = characterRPGList.get(position);
            holder.textViewName.setText(characterRPG.getName());
            holder.textViewRace.setText(characterRPG.getRace());
            holder.textViewClass.setText(characterRPG.getCharacterClass());
            holder.textViewLevel.setText("Level " + characterRPG.getLevel());
            holder.textViewGameVersion.setText(characterRPG.getGameVersion());
            loadMiniature(holder.imageViewMiniature, characterRPG.getCharacterMiniaturePath());

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCharacterClick(characterRPG);
                }
            });
        } else {
            holder.textViewName.setText("Add New Character");
            holder.textViewRace.setText("");
            holder.textViewClass.setText("");
            holder.textViewLevel.setText("");
            holder.textViewGameVersion.setText("");
            holder.imageViewMiniature.setImageResource(R.drawable.ic_add);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddCharacterClick();
                }
            });
        }
    }

    private void loadMiniature(ImageView imageView, String miniaturePath) {
        if (miniaturePath != null) {
            if (miniaturePath.startsWith("assets://")) {
                try {
                    InputStream is = imageView.getContext().getAssets().open(
                            miniaturePath.replace("assets://", ""));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    imageView.setImageResource(R.drawable.default_character_image);
                }
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(miniaturePath);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.default_character_image);
                }
            }
        } else {
            imageView.setImageResource(R.drawable.default_character_image);
        }
    }

    @Override
    public int getItemCount() {
        return characterRPGList.size() + 1;
    }

    static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewRace, textViewClass, textViewLevel, textViewGameVersion;
        ImageView imageViewMiniature;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewRace = itemView.findViewById(R.id.textViewRace);
            textViewClass = itemView.findViewById(R.id.textViewClass);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
            textViewGameVersion = itemView.findViewById(R.id.textViewGameVersion);
            imageViewMiniature = itemView.findViewById(R.id.imageViewMiniature);
        }
    }

    public interface  OnCharacterClickListener  {
        void onCharacterClick(CharacterRPG characterRPG);
        void onAddCharacterClick();
    }
}

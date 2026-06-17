package com.fizzycoyote.qusetroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.character.CharacterEntity;

import java.util.ArrayList;
import java.util.List;

public class CharacterForCampaignAdapter extends RecyclerView.Adapter<CharacterForCampaignAdapter.ViewHolder> {

    private List<CharacterEntity> characters = new ArrayList<>();
    private final OnCharacterClickListener listener;

    public interface OnCharacterClickListener {
        void onRemoveClick(CharacterEntity character);
    }

    public CharacterForCampaignAdapter(OnCharacterClickListener listener) {
        this.listener = listener;
    }

    public void setCharacters(List<CharacterEntity> characters) {
        this.characters = characters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterEntity character = characters.get(position);
        holder.tvName.setText(character.name);
        holder.btnRemove.setOnClickListener(v -> listener.onRemoveClick(character));
    }

    @Override
    public int getItemCount() { return characters.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        View btnRemove;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_character_name);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
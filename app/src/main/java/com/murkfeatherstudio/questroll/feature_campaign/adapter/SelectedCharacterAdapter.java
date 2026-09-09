package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
import com.murkfeatherstudio.questroll.databinding.ItemSelectedCharacterBinding;

import java.util.ArrayList;
import java.util.List;

public class SelectedCharacterAdapter extends RecyclerView.Adapter<SelectedCharacterAdapter.ViewHolder> {

    private List<CharacterEntity> characters = new ArrayList<>();
    private final OnRemoveClickListener removeListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(CharacterEntity character);
    }

    public SelectedCharacterAdapter(OnRemoveClickListener listener) {
        this.removeListener = listener;
    }

    public void setCharacters(List<CharacterEntity> characters) {
        this.characters = characters;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSelectedCharacterBinding binding = ItemSelectedCharacterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterEntity character = characters.get(position);
        holder.binding.tvCharacterName.setText(character.name);
        holder.binding.btnRemove.setOnClickListener(v -> removeListener.onRemoveClick(character));
    }

    @Override
    public int getItemCount() { return characters.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectedCharacterBinding binding;
        ViewHolder(ItemSelectedCharacterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
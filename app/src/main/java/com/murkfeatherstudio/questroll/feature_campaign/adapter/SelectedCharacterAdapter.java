package com.murkfeatherstudio.questroll.feature_campaign.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.character.CharacterEntity;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_character, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CharacterEntity character = characters.get(position);
        holder.tvName.setText(character.name);
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemoveClick(character));
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
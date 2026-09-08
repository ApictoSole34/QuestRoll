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

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {

    private List<CharacterEntity> characters = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CharacterEntity character);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
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
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(character);
        });
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_character_name);
        }
    }
}
package com.murkfeatherstudio.questroll.feature_spell.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.models.custom.custom_spell.CustomCastingOption;

import java.util.ArrayList;
import java.util.List;

public class CastingOptionAdapter extends RecyclerView.Adapter<CastingOptionAdapter.ViewHolder> {

    private List<CustomCastingOption> options = new ArrayList<>();
    private final OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public CastingOptionAdapter(OnDeleteListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CustomCastingOption> newOptions) {
        options = newOptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_casting_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(options.get(position), position, listener);
    }

    @Override
    public int getItemCount() { return options.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_option_type);
            tvDetails = itemView.findViewById(R.id.tv_option_details);
            btnDelete = itemView.findViewById(R.id.btn_delete_option);
        }

        void bind(CustomCastingOption option, int position, OnDeleteListener listener) {
            tvType.setText(option.type);

            StringBuilder details = new StringBuilder();
            if (!option.damageRoll.isEmpty()) details.append("Damage: ").append(option.damageRoll);
            if (!option.range.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Range: ").append(option.range);
            }
            if (!option.duration.isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Duration: ").append(option.duration);
            }
            if (!option.desc.isEmpty()) {
                if (details.length() > 0) details.append("\n");
                details.append(option.desc);
            }

            tvDetails.setText(details.toString());
            tvDetails.setVisibility(details.length() > 0 ? View.VISIBLE : View.GONE);

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(position);
            });
        }
    }
}

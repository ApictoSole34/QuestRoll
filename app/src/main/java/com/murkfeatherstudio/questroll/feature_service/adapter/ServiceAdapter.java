package com.murkfeatherstudio.questroll.feature_service.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.feature_service.model.CombinedService;

public class ServiceAdapter extends ListAdapter<CombinedService, ServiceAdapter.ViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CombinedService service);
    }

    public ServiceAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CombinedService>() {
            @Override
            public boolean areItemsTheSame(@NonNull CombinedService a, @NonNull CombinedService b) {
                return a.id.equals(b.id);
            }
            @Override
            public boolean areContentsTheSame(@NonNull CombinedService a, @NonNull CombinedService b) {
                return a.name.equals(b.name);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCostDetail, tvDesc, tvCustomBadge;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvCostDetail = v.findViewById(R.id.tv_cost_detail);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvCustomBadge = v.findViewById(R.id.tv_custom_badge);
        }

        void bind(CombinedService s, OnItemClickListener listener) {
            tvName.setText(s.name);
            tvCostDetail.setText(s.cost + " gp / " + s.detail);
            tvDesc.setText(s.desc != null ? (s.desc.length() > 100 ? s.desc.substring(0, 100) + "…" : s.desc) : "");
            tvCustomBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
            itemView.setOnClickListener(v -> listener.onItemClick(s));
        }
    }
}
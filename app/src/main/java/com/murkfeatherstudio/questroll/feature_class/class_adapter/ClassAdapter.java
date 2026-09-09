package com.murkfeatherstudio.questroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemClassListBinding;
import com.murkfeatherstudio.questroll.databinding.ItemCreateClassBinding;
import com.murkfeatherstudio.questroll.feature_class.model.CombinedClass;

import java.util.ArrayList;
import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CREATE = 0;
    private static final int TYPE_CLASS = 1;

    private List<CombinedClass> classes = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClassClick(CombinedClass classEntity);
        void onCreateClick();
    }

    public ClassAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CREATE) {
            ItemCreateClassBinding binding = ItemCreateClassBinding.inflate(inflater, parent, false);
            return new CreateViewHolder(binding);
        }

        ItemClassListBinding binding = ItemClassListBinding.inflate(inflater, parent, false);
        return new ClassViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CREATE) {
            ((CreateViewHolder)holder).bind(listener);
        } else {
            int classPosition = position - 1;
            ((ClassViewHolder)holder).bind(classes.get(classPosition), listener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_CREATE : TYPE_CLASS;
    }

    @Override
    public int getItemCount() {
        return classes.size() + 1;
    }

    public void submitList(List<CombinedClass> newClasses) {
        classes = new ArrayList<>(newClasses);
        notifyDataSetChanged();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        private final ItemClassListBinding binding;

        ClassViewHolder(ItemClassListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedClass item, OnItemClickListener listener) {
            binding.tvClassName.setText(item.name);

            if (item.parentName != null && !item.parentName.isEmpty()) {
                binding.tvSubclass.setVisibility(View.VISIBLE);
                binding.tvSubclass.setText("Subclass of " + item.parentName);
            } else {
                binding.tvSubclass.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onClassClick(item);
                }
            });
        }
    }

    static class CreateViewHolder extends RecyclerView.ViewHolder {
        CreateViewHolder(ItemCreateClassBinding binding) {
            super(binding.getRoot());
        }

        void bind(OnItemClickListener listener) {
            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() == 0) {
                    listener.onCreateClick();
                }
            });
        }
    }
}

package com.fizzycoyote.qusetroll.feature_class.class_adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.feature_class.model.CombinedClass;

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
        if (viewType == TYPE_CREATE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_create_class, parent, false);
            return new CreateViewHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class_list, parent, false);
        return new ClassViewHolder(view);
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
        private final TextView tvClassName;
        private final TextView tvSubclass;

        ClassViewHolder(View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tv_class_name);
            tvSubclass = itemView.findViewById(R.id.tv_subclass);
        }

        void bind(CombinedClass item, OnItemClickListener listener) {
            tvClassName.setText(item.name);

            if (item.parentName != null && !item.parentName.isEmpty()) {
                tvSubclass.setVisibility(View.VISIBLE);
                tvSubclass.setText("Subclass of " + item.parentName);
            } else {
                tvSubclass.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onClassClick(item);
                }
            });
        }
    }

    static class CreateViewHolder extends RecyclerView.ViewHolder {
        CreateViewHolder(View itemView) {
            super(itemView);
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
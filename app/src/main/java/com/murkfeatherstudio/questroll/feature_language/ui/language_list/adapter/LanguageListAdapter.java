package com.murkfeatherstudio.questroll.feature_language.ui.language_list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.databinding.ItemCreateLanguageBinding;
import com.murkfeatherstudio.questroll.databinding.ItemLanguageBinding;
import com.murkfeatherstudio.questroll.feature_language.model.CombinedLanguage;

import java.util.List;
import java.util.Objects;

public class LanguageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_CREATE = 0;
    private static final int TYPE_ITEM   = 1;

    public interface OnLanguageClickListener {
        void onLanguageClick(CombinedLanguage language);
    }
    public interface OnCreateClickListener {
        void onCreateClick();
    }

    private final OnLanguageClickListener languageListener;
    private final OnCreateClickListener   createListener;

    public LanguageListAdapter(@NonNull List<CombinedLanguage> items,
                               @NonNull OnLanguageClickListener languageListener,
                               @NonNull OnCreateClickListener createListener) {
        this.languageListener = languageListener;
        this.createListener = createListener;
        differ.submitList(items);
    }
    private final DiffUtil.ItemCallback<CombinedLanguage> diffCallback =
            new DiffUtil.ItemCallback<CombinedLanguage>() {
                @Override
                public boolean areItemsTheSame(@NonNull CombinedLanguage oldItem, @NonNull CombinedLanguage newItem) {
                    return Objects.equals(oldItem.getUniqueKey(), newItem.getUniqueKey());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CombinedLanguage oldItem, @NonNull CombinedLanguage newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private final AsyncListDiffer<CombinedLanguage> differ =
            new AsyncListDiffer<>(this, diffCallback);

    public void updateList(@NonNull List<CombinedLanguage> newItems) {
        differ.submitList(newItems);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size() + 1;
    }

    @Override public int getItemViewType(int position) {
        return position == 0 ? TYPE_CREATE : TYPE_ITEM;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_CREATE) {
            ItemCreateLanguageBinding binding = ItemCreateLanguageBinding.inflate(inf, parent, false);
            return new CreateViewHolder(binding);
        } else {
            ItemLanguageBinding binding = ItemLanguageBinding.inflate(inf, parent, false);
            return new LanguageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CREATE) {
            ((CreateViewHolder) holder).bind(createListener);
        } else {
            CombinedLanguage lang = differ.getCurrentList().get(position - 1);
            ((LanguageViewHolder) holder).bind(lang, languageListener);
        }
    }

    static class CreateViewHolder extends RecyclerView.ViewHolder {
        CreateViewHolder(ItemCreateLanguageBinding binding) {
            super(binding.getRoot());
        }
        void bind(OnCreateClickListener listener) {
            itemView.setOnClickListener(v -> listener.onCreateClick());
        }
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        private final ItemLanguageBinding binding;

        LanguageViewHolder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CombinedLanguage lang, OnLanguageClickListener listener) {
            binding.tvName.setText(lang.getName());

            StringBuilder flags = new StringBuilder();
            if (lang.isExotic()) flags.append("[Exotic] ");
            if (lang.isSecret()) flags.append("[Secret]");
            binding.tvFlags.setText(flags.toString());

            if (lang.getScriptLanguageName() != null &&
                    !lang.getScriptLanguageName().isEmpty() &&
                    !lang.getScriptLanguageName().equals(lang.getName())) {

                binding.tvScript.setText("Script: " + lang.getScriptLanguageName());
                binding.tvScript.setVisibility(View.VISIBLE);
            } else {
                binding.tvScript.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onLanguageClick(lang));
        }
    }
}

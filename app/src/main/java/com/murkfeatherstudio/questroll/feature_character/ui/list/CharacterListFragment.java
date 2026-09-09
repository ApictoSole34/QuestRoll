package com.murkfeatherstudio.questroll.feature_character.ui.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentCharacterListBinding;
import com.murkfeatherstudio.questroll.databinding.ItemCharacterBinding;
import com.murkfeatherstudio.questroll.feature_character.model.CharacterDisplay;
import com.murkfeatherstudio.questroll.feature_character.ui.wizard.CharacterWizardActivity;
import com.murkfeatherstudio.questroll.feature_character.view_model.CharacterListViewModel;

import java.io.File;
import java.util.List;

public class CharacterListFragment extends Fragment {

    private CharacterListViewModel viewModel;
    private CharacterAdapter adapter;
    private FragmentCharacterListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCharacterListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);

        binding.characterRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CharacterAdapter(display -> {
            Bundle args = new Bundle();
            args.putLong("character_id", display.id);
            Navigation.findNavController(view).navigate(R.id.action_to_sheet, args);
        });
        binding.characterRecycler.setAdapter(adapter);

        viewModel.getDisplays().observe(getViewLifecycleOwner(), displays -> adapter.setCharacters(displays));

        binding.btnNewCharacter.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CharacterWizardActivity.class)));
    }

    private static class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {
        private List<CharacterDisplay> characters = List.of();
        private final OnCharacterClickListener listener;

        CharacterAdapter(OnCharacterClickListener listener) { this.listener = listener; }

        void setCharacters(List<CharacterDisplay> chars) {
            this.characters = chars;
            notifyDataSetChanged();
        }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCharacterBinding binding = ItemCharacterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterDisplay c = characters.get(position);
            holder.binding.characterName.setText(c.name);
            holder.binding.characterClassLevel.setText(c.className);
            holder.binding.characterRaceSystem.setText(c.raceName + " • " + c.gameSystem);

            // Improved avatar loading (User Photo or Default AI Asset)
            if (c.thumbnailPath != null && !c.thumbnailPath.isEmpty() && new File(c.thumbnailPath).exists()) {
                Glide.with(holder.binding.characterAvatar.getContext()).load(new File(c.thumbnailPath)).into(holder.binding.characterAvatar);
            } else {
                // Loading default AI image from assets
                Glide.with(holder.binding.characterAvatar.getContext())
                        .load(Uri.parse("file:///android_asset/characters_miniatures/ai-generated-9221232_1920_mini.png"))
                        .into(holder.binding.characterAvatar);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(c));
        }

        @Override public int getItemCount() { return characters.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemCharacterBinding binding;
            ViewHolder(ItemCharacterBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        interface OnCharacterClickListener { void onClick(CharacterDisplay character); }
    }
}

package com.fizzycoyote.qusetroll.feature_character.ui.list;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.model.CharacterDisplay;
import com.fizzycoyote.qusetroll.feature_character.ui.wizard.CharacterWizardActivity;
import com.fizzycoyote.qusetroll.feature_character.view_model.CharacterListViewModel;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CharacterListFragment extends Fragment {

    private CharacterListViewModel viewModel;
    private RecyclerView recyclerView;
    private CharacterAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);

        recyclerView = view.findViewById(R.id.character_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CharacterAdapter(display -> {
            Bundle args = new Bundle();
            args.putLong("character_id", display.id);
            Navigation.findNavController(view).navigate(R.id.action_to_sheet, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getDisplays().observe(getViewLifecycleOwner(), displays -> {
            adapter.setCharacters(displays);
        });

        view.findViewById(R.id.btn_new_character).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CharacterWizardActivity.class));
        });
    }

    private static class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.ViewHolder> {
        private List<CharacterDisplay> characters = List.of();
        private final OnCharacterClickListener listener;

        CharacterAdapter(OnCharacterClickListener listener) { this.listener = listener; }

        void setCharacters(List<CharacterDisplay> chars) {
            this.characters = chars;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterDisplay c = characters.get(position);
            holder.nameView.setText(c.name);
            holder.classLevelView.setText(c.className);
            holder.raceSystemView.setText(c.raceName + " • " + c.gameSystem);

            // Load thumbnail
            if (c.thumbnailPath != null && !c.thumbnailPath.isEmpty()) {
                File thumbFile = new File(c.thumbnailPath);
                if (thumbFile.exists()) {
                    holder.avatarView.setImageURI(Uri.fromFile(thumbFile));
                } else {
                    holder.avatarView.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.avatarView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Click listener to open character sheet
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(c);
                }
            });
        }

        @Override
        public int getItemCount() { return characters.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView avatarView;
            TextView nameView;
            TextView classLevelView;
            TextView raceSystemView;

            ViewHolder(View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.character_avatar);
                nameView = itemView.findViewById(R.id.character_name);
                classLevelView = itemView.findViewById(R.id.character_class_level);
                raceSystemView = itemView.findViewById(R.id.character_race_system);
            }
        }

        interface OnCharacterClickListener {
            void onClick(CharacterDisplay character);
        }
    }
}
package com.murkfeatherstudio.questroll.feature_character.ui.list;

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

import com.bumptech.glide.Glide;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.feature_character.model.CharacterDisplay;
import com.murkfeatherstudio.questroll.feature_character.ui.wizard.CharacterWizardActivity;
import com.murkfeatherstudio.questroll.feature_character.view_model.CharacterListViewModel;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CharacterListFragment extends Fragment {

    private CharacterListViewModel viewModel;
    private CharacterAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_character_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CharacterListViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.character_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CharacterAdapter(display -> {
            Bundle args = new Bundle();
            args.putLong("character_id", display.id);
            Navigation.findNavController(view).navigate(R.id.action_to_sheet, args);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getDisplays().observe(getViewLifecycleOwner(), displays -> adapter.setCharacters(displays));

        view.findViewById(R.id.btn_new_character).setOnClickListener(v -> 
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
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CharacterDisplay c = characters.get(position);
            holder.nameView.setText(c.name);
            holder.classLevelView.setText(c.className);
            holder.raceSystemView.setText(c.raceName + " • " + c.gameSystem);

            // Poprawione ładowanie awatara (User Photo lub Default AI Asset)
            if (c.thumbnailPath != null && !c.thumbnailPath.isEmpty() && new File(c.thumbnailPath).exists()) {
                Glide.with(holder.avatarView.getContext()).load(new File(c.thumbnailPath)).into(holder.avatarView);
            } else {
                // Ładowanie domyślnego obrazka AI z assets
                Glide.with(holder.avatarView.getContext())
                        .load(Uri.parse("file:///android_asset/characters_miniatures/ai-generated-9221232_1920_mini.png"))
                        .into(holder.avatarView);
            }

            holder.itemView.setOnClickListener(v -> listener.onClick(c));
        }

        @Override public int getItemCount() { return characters.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            CircleImageView avatarView;
            TextView nameView, classLevelView, raceSystemView;
            ViewHolder(View v) {
                super(v);
                avatarView = v.findViewById(R.id.character_avatar);
                nameView = v.findViewById(R.id.character_name);
                classLevelView = v.findViewById(R.id.character_class_level);
                raceSystemView = v.findViewById(R.id.character_race_system);
            }
        }
        interface OnCharacterClickListener { void onClick(CharacterDisplay character); }
    }
}

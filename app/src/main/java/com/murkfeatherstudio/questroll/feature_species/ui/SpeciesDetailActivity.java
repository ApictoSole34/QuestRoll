package com.murkfeatherstudio.questroll.feature_species.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesDto;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.murkfeatherstudio.questroll.databinding.ActivitySpeciesDetailBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class SpeciesDetailActivity extends BaseActivity {

    private Markwon markwon;
    private ActivitySpeciesDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpeciesDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("SPECIES_KEY");
        Open5eDatabase.getInstance(this).speciesDao()
                .getByKey(key).observe(this, s -> { if (s != null) populateUI(s); });
    }

    private void populateUI(SpeciesEntity s) {
        binding.tvSpeciesName.setText(s.name);

        if (s.isSubspecies && s.subspeciesOf != null && !s.subspeciesOf.isEmpty()) {
            binding.tvSubtitle.setText("Subspecies of " + s.subspeciesOf);
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setPaintFlags(binding.tvSubtitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            binding.tvSubtitle.setOnClickListener(v -> {
                Intent i = new Intent(this, SpeciesDetailActivity.class);
                i.putExtra("SPECIES_KEY", s.subspeciesOf);
                startActivity(i);
            });
        } else if (s.documentName != null) {
            binding.tvSubtitle.setText(s.documentName);
            binding.tvSubtitle.setVisibility(View.VISIBLE);
            binding.tvSubtitle.setOnClickListener(null);
            binding.tvSubtitle.setPaintFlags(binding.tvSubtitle.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        } else {
            binding.tvSubtitle.setVisibility(View.GONE);
        }

        if (s.desc != null && !s.desc.isEmpty()) {
            markwon.setMarkdown(binding.tvDesc, s.desc);
            binding.tvDesc.setVisibility(View.VISIBLE);
        } else {
            binding.tvDesc.setVisibility(View.GONE);
        }

        buildTraits(s.traitsJson);
        buildSubspecies(s.key);

        binding.btnManage.setVisibility(View.GONE);
    }

    /**
     * NOTE: traits_container is managed dynamically (addView()).
     * Views for individual traits are created at runtime based on JSON data.
     * ViewBinding is not applicable to these dynamically generated children.
     */
    private void buildTraits(String traitsJson) {
        binding.traitsContainer.removeAllViews();

        if (traitsJson == null || traitsJson.isEmpty()) {
            binding.traitsSection.setVisibility(View.GONE);
            return;
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SpeciesDto.SpeciesTraitDto>>(){}.getType();
            List<SpeciesDto.SpeciesTraitDto> traits = gson.fromJson(traitsJson, type);
            if (traits == null || traits.isEmpty()) {
                binding.traitsSection.setVisibility(View.GONE);
                return;
            }

            binding.traitsSection.setVisibility(View.VISIBLE);
            for (SpeciesDto.SpeciesTraitDto trait : traits) {
                addTraitView(binding.traitsContainer, trait.name, trait.desc);
            }
        } catch (Exception e) {
            binding.traitsSection.setVisibility(View.GONE);
        }
    }

    /**
     * NOTE: subspecies_container is managed dynamically (addView()).
     * Views for subspecies are created at runtime based on MediatorLiveData query results.
     * ViewBinding is not applicable to these dynamically generated children.
     */
    private void buildSubspecies(String parentKey) {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        LiveData<List<SpeciesEntity>> open5eSubs = open5eDb.speciesDao().getSubspeciesOf(parentKey);
        LiveData<List<CustomSpeciesEntity>> customSubs = customDb.customSpeciesDao().getAll();

        MediatorLiveData<List<Object>> mediator = new MediatorLiveData<>();

        Observer<Object> combine = ignored -> {
            List<Object> all = new ArrayList<>();
            if (open5eSubs.getValue() != null) all.addAll(open5eSubs.getValue());
            if (customSubs.getValue() != null) {
                for (CustomSpeciesEntity cs : customSubs.getValue()) {
                    if (parentKey.equals(cs.subspeciesOfKey)
                            || ("custom_" + cs.id).equals(cs.subspeciesOfKey)) {
                        all.add(cs);
                    }
                }
            }
            mediator.setValue(all);
        };

        mediator.addSource(open5eSubs, s -> combine.onChanged(null));
        mediator.addSource(customSubs, s -> combine.onChanged(null));

        mediator.observe(this, all -> {
            binding.subspeciesContainer.removeAllViews();
            if (all == null || all.isEmpty()) {
                binding.subspeciesSection.setVisibility(View.GONE);
                return;
            }
            binding.subspeciesSection.setVisibility(View.VISIBLE);
            for (Object item : all) {
                TextView tv = new TextView(this);
                tv.setPadding(0, dp(6), 0, dp(6));
                tv.setTextSize(15);
                tv.setTextColor(getResources().getColor(com.murkfeatherstudio.questroll.R.color.threads_text_primary, null));

                if (item instanceof SpeciesEntity) {
                    SpeciesEntity sub = (SpeciesEntity) item;
                    tv.setText("• " + sub.name);
                    tv.setOnClickListener(v -> {
                        Intent i = new Intent(this, SpeciesDetailActivity.class);
                        i.putExtra("SPECIES_KEY", sub.key);
                        startActivity(i);
                    });
                } else if (item instanceof CustomSpeciesEntity) {
                    CustomSpeciesEntity sub = (CustomSpeciesEntity) item;
                    tv.setText("• " + sub.name + " (Custom)");
                    tv.setOnClickListener(v -> {
                        Intent i = new Intent(this, CustomSpeciesDetailActivity.class);
                        i.putExtra("CUSTOM_SPECIES_ID", sub.id);
                        startActivity(i);
                    });
                }
                binding.subspeciesContainer.addView(tv);
            }
        });
    }

    private void addTraitView(LinearLayout container, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setTextSize(15);
        tvName.setTextColor(getResources().getColor(com.murkfeatherstudio.questroll.R.color.threads_gold, null));
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
            tvDesc.setTextColor(getResources().getColor(com.murkfeatherstudio.questroll.R.color.threads_text_primary, null));
            row.addView(tvDesc);
        }
        container.addView(row);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}

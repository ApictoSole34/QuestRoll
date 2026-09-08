package com.murkfeatherstudio.questroll.feature_species.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.local_database.UserContentDatabase;
import com.murkfeatherstudio.questroll.core.models.custom.custom_species.CustomSpeciesEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesDto;
import com.murkfeatherstudio.questroll.core.models.open5e.species.SpeciesEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

public class SpeciesDetailActivity extends BaseActivity {

    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_species_detail);
        markwon = Markwon.create(this);

        String key = getIntent().getStringExtra("SPECIES_KEY");
        Open5eDatabase.getInstance(this).speciesDao()
                .getByKey(key).observe(this, s -> { if (s != null) populateUI(s); });
    }

    private void populateUI(SpeciesEntity s) {
        ((TextView) findViewById(R.id.tv_species_name)).setText(s.name);

        TextView tvSubtitle = findViewById(R.id.tv_subtitle);
        if (s.isSubspecies && s.subspeciesOf != null && !s.subspeciesOf.isEmpty()) {
            tvSubtitle.setText("Subspecies of " + s.subspeciesOf);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setPaintFlags(tvSubtitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvSubtitle.setOnClickListener(v -> {
                Intent i = new Intent(this, SpeciesDetailActivity.class);
                i.putExtra("SPECIES_KEY", s.subspeciesOf);
                startActivity(i);
            });
        } else if (s.documentName != null) {
            tvSubtitle.setText(s.documentName);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setOnClickListener(null);
            tvSubtitle.setPaintFlags(tvSubtitle.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
        } else {
            tvSubtitle.setVisibility(View.GONE);
        }

        TextView tvDesc = findViewById(R.id.tv_desc);
        if (s.desc != null && !s.desc.isEmpty()) {
            markwon.setMarkdown(tvDesc, s.desc);
            tvDesc.setVisibility(View.VISIBLE);
        } else {
            tvDesc.setVisibility(View.GONE);
        }

        buildTraits(s.traitsJson);
        buildSubspecies(s.key);
    }

    private void buildTraits(String traitsJson) {
        LinearLayout container = findViewById(R.id.traits_container);
        container.removeAllViews();

        if (traitsJson == null || traitsJson.isEmpty()) {
            findViewById(R.id.traits_section).setVisibility(View.GONE);
            return;
        }

        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<SpeciesDto.SpeciesTraitDto>>(){}.getType();
            List<SpeciesDto.SpeciesTraitDto> traits = gson.fromJson(traitsJson, type);
            if (traits == null || traits.isEmpty()) {
                findViewById(R.id.traits_section).setVisibility(View.GONE);
                return;
            }

            findViewById(R.id.traits_section).setVisibility(View.VISIBLE);
            for (SpeciesDto.SpeciesTraitDto trait : traits) {
                addTraitView(container, trait.name, trait.desc);
            }
        } catch (Exception e) {
            findViewById(R.id.traits_section).setVisibility(View.GONE);
        }
    }

    private void buildSubspecies(String parentKey) {
        LinearLayout section = findViewById(R.id.subspecies_section);
        LinearLayout container = findViewById(R.id.subspecies_container);

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
            container.removeAllViews();
            if (all == null || all.isEmpty()) {
                section.setVisibility(View.GONE);
                return;
            }
            section.setVisibility(View.VISIBLE);
            for (Object item : all) {
                TextView tv = new TextView(this);
                tv.setPadding(0, dp(6), 0, dp(6));
                tv.setTextSize(15);

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
                container.addView(tv);
            }
        });
    }

    private void addTraitView(LinearLayout container, String name, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(8), 0, dp(4));

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextSize(15);
        row.addView(tvName);

        if (desc != null && !desc.isEmpty()) {
            TextView tvDesc = new TextView(this);
            markwon.setMarkdown(tvDesc, desc);
            tvDesc.setTextSize(14);
            row.addView(tvDesc);
        }
        container.addView(row);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}
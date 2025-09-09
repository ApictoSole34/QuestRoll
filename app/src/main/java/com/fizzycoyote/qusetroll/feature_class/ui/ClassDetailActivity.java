package com.fizzycoyote.qusetroll.feature_class.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.local_database.UserContentDatabase;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.CustomCharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.custom.custom_character_class.custom_feature.CustomFeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.CharacterClassEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.feature.FeatureEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.character_class.hit_points.HitPointsEntity;
import com.fizzycoyote.qusetroll.feature_class.class_adapter.FeatureAdapter;
import com.fizzycoyote.qusetroll.feature_class.view_model.ClassDetailViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class ClassDetailActivity extends AppCompatActivity {


    private TextView className, hitDiceTextView, casterType;
    private RecyclerView featuresRecycler;
    private ClassDetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        String classKey = getIntent().getStringExtra("CLASS_KEY");
        initViews();
        setupViewModel(classKey);
        observeData();
    }

    private void initViews() {
        className = findViewById(R.id.tv_class_name);
        hitDiceTextView = findViewById(R.id.tv_hit_dice);
        casterType = findViewById(R.id.tv_caster_type);
        featuresRecycler = findViewById(R.id.recycler_features);
        featuresRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupViewModel(String classKey) {
        Open5eDatabase open5eDb = Open5eDatabase.getInstance(this);
        UserContentDatabase customDb = UserContentDatabase.getInstance(this);

        ClassDetailViewModel.Factory factory = new ClassDetailViewModel.Factory(
                open5eDb.characterClassDao(),
                open5eDb.featureDao(),
                open5eDb.hitPointsDao(),
                customDb.customCharacterClassDao(),
                classKey
        );

        viewModel = new ViewModelProvider(this, factory).get(ClassDetailViewModel.class);
    }

    private void observeData() {
        if (viewModel.isCustom()) {
            observeCustomData();
        } else {
            observeOpen5eData();
        }
    }

    private void observeOpen5eData() {
        viewModel.getOpen5eClass().observe(this, this::updateOpen5eClassInfo);
        viewModel.getOpen5eFeatures().observe(this, this::updateFeatures);
        viewModel.getOpen5eHitPoints().observe(this, this::updateHitPoints);
    }

    private void observeCustomData() {
        viewModel.getCustomClass().observe(this, data -> {
            if (data != null) {
                updateCustomClassInfo(data.characterClassEntity);
            }
        });
    }

    private void updateOpen5eClassInfo(CharacterClassEntity entity) {
        if (entity == null) return;
        className.setText(entity.name);
        casterType.setText(entity.casterType != null ?
                "Caster Type: " + entity.casterType : "");
    }

    private void updateCustomClassInfo(CustomCharacterClassEntity entity) {
        if (entity == null) return;
        className.setText(entity.name);
        casterType.setText(entity.casterType != null ?
                "Caster Type: " + entity.casterType : "");
        hitDiceTextView.setText("Hit Dice: " + entity.hitDice);
    }

    private void updateHitPoints(HitPointsEntity hitPoints) {
        if (hitPoints != null) {
            hitDiceTextView.setText("Hit Dice: " + hitPoints.hitDice);
        }
    }

    private void updateFeatures(List<FeatureEntity> features) {
        FeatureAdapter adapter = new FeatureAdapter();
        featuresRecycler.setAdapter(adapter);
        adapter.submitList(convertToCustomFeatures(features));
    }

    private List<CustomFeatureEntity> convertToCustomFeatures(List<FeatureEntity> features) {
        return features.stream().map(f -> {
            CustomFeatureEntity customFeature = new CustomFeatureEntity();
            customFeature.name = f.name;
            customFeature.description = f.desc;
            customFeature.type = f.featureType;
            return customFeature;
        }).collect(Collectors.toList());
    }
}
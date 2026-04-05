package com.fizzycoyote.qusetroll.feature_alignment.ui;

import static com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment.formatAlignmentKey;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.alignment.AlignmentEntity;
import com.fizzycoyote.qusetroll.feature_alignment.model.CombinedAlignment;

public class AlignmentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alignment_detail);

        String key = getIntent().getStringExtra("ALIGNMENT_KEY");
        if (key == null) {
            finish();
            return;
        }

        Open5eDatabase.getInstance(this).alignmentDao()
                .getByKey(key).observe(this, alignment -> {
                    if (alignment != null) populateUI(alignment);
                });
    }

    private void populateUI(AlignmentEntity a) {
        String fullName = CombinedAlignment.formatAlignmentKey(a.key);
        ((TextView) findViewById(R.id.tv_name)).setText(fullName);
        ((TextView) findViewById(R.id.tv_short)).setText(a.shortName != null ? "(" + a.shortName + ")" : "");
        ((TextView) findViewById(R.id.tv_morality)).setText("Morality: " + (a.morality != null ? a.morality : ""));
        ((TextView) findViewById(R.id.tv_attitude)).setText("Attitude: " + (a.societalAttitude != null ? a.societalAttitude : ""));
        ((TextView) findViewById(R.id.tv_description)).setText(a.description != null ? a.description : "");
        findViewById(R.id.btnManage).setVisibility(View.GONE);
    }
}
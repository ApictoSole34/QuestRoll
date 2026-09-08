package com.murkfeatherstudio.questroll.main.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}

package com.murkfeatherstudio.questroll.feature_tools.pdf.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.core.models.custom.pdf.CustomPdfEntity;
import com.murkfeatherstudio.questroll.feature_tools.pdf.adapter.PdfAdapter;
import com.murkfeatherstudio.questroll.feature_tools.pdf.viewmodel.PdfListViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PdfListActivity extends BaseActivity {

    private PdfListViewModel viewModel;
    private PdfAdapter adapter;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // We could extract the real name from DocumentFile or cursor
                        String fileName = "Manual_" + System.currentTimeMillis() + ".pdf";
                        viewModel.addPdf(fileName, uri);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to get permission for this file.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list);

        viewModel = new ViewModelProvider(this).get(PdfListViewModel.class);
        
        RecyclerView recyclerView = findViewById(R.id.rv_pdfs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new PdfAdapter(
                this::openPdf,
                pdf -> viewModel.deletePdf(pdf)
        );
        recyclerView.setAdapter(adapter);

        viewModel.getAllPdfs().observe(this, adapter::submitList);

        FloatingActionButton fab = findViewById(R.id.fab_add_pdf);
        fab.setOnClickListener(v -> pdfPickerLauncher.launch(new String[]{"application/pdf"}));
    }

    private void openPdf(CustomPdfEntity pdf) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_URI, pdf.uri);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, pdf.name);
        startActivity(intent);
    }
}

package com.murkfeatherstudio.questroll.feature_tools.pdf.ui;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.murkfeatherstudio.questroll.core.base.BaseActivity;
import com.murkfeatherstudio.questroll.databinding.ActivityPdfViewerBinding;

import java.io.IOException;

public class PdfViewerActivity extends BaseActivity {

    public static final String EXTRA_PDF_URI = "extra_pdf_uri";
    public static final String EXTRA_PDF_TITLE = "extra_pdf_title";

    private ActivityPdfViewerBinding binding;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor fileDescriptor;
    private int pageIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String uriString = getIntent().getStringExtra(EXTRA_PDF_URI);
        String title = getIntent().getStringExtra(EXTRA_PDF_TITLE);
        binding.tvPdfTitle.setText(title != null ? title : "PDF Viewer");

        binding.btnBack.setOnClickListener(v -> finish());

        if (uriString != null) {
            openPdf(Uri.parse(uriString));
        }

        binding.btnPrev.setOnClickListener(v -> showPage(pageIndex - 1));
        binding.btnNext.setOnClickListener(v -> showPage(pageIndex + 1));
    }

    private void openPdf(Uri uri) {
        try {
            fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                pdfRenderer = new PdfRenderer(fileDescriptor);
                showPage(0);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error opening PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer == null || index < 0 || index >= pdfRenderer.getPageCount()) return;

        if (currentPage != null) {
            currentPage.close();
        }

        pageIndex = index;
        currentPage = pdfRenderer.openPage(index);

        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth() * 2, currentPage.getHeight() * 2, Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        binding.pdfPageView.setImageBitmap(bitmap);

        binding.tvPageInfo.setText(String.format("Page %d/%d", index + 1, pdfRenderer.getPageCount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (fileDescriptor != null) fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

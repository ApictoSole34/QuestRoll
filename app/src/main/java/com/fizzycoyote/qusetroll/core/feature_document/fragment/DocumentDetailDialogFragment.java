package com.fizzycoyote.qusetroll.core.feature_document.fragment;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.core.local_database.Open5eDatabase;
import com.fizzycoyote.qusetroll.core.models.open5e.document.DocumentEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.game_system.GameSystemEntity;
import com.fizzycoyote.qusetroll.core.models.open5e.license.LicenseEntity;

import java.util.concurrent.Executors;

public class DocumentDetailDialogFragment extends DialogFragment {
    private static final String ARG_URL = "arg_url";

    public static DocumentDetailDialogFragment newInstance(String documentUrl) {
        Bundle args = new Bundle();
        args.putString(ARG_URL, documentUrl);
        DocumentDetailDialogFragment frag = new DocumentDetailDialogFragment();
        frag.setArguments(args);
        return frag;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String url = requireArguments().getString(ARG_URL);
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_document_detail, null);

        TextView tvName       = view.findViewById(R.id.tv_doc_name);
        TextView tvAuthor     = view.findViewById(R.id.tv_doc_author);
        TextView tvLicense    = view.findViewById(R.id.tv_doc_license);
        TextView tvGameSystem = view.findViewById(R.id.tv_doc_gamesystem);
        TextView tvDistance   = view.findViewById(R.id.tv_doc_distance);
        TextView tvPublished  = view.findViewById(R.id.tv_doc_published);
        TextView tvPermalink  = view.findViewById(R.id.tv_doc_permalink);
        TextView tvDesc       = view.findViewById(R.id.tv_doc_desc);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Close", null)
                .create();

        Executors.newSingleThreadExecutor().execute(() -> {
            Open5eDatabase db = Open5eDatabase.getInstance(requireContext());
            DocumentEntity doc = db.documentDao().getByUrl(url);

            String name        = doc != null ? doc.name : "—";
            String author      = doc != null && doc.author != null        ? doc.author      : "—";
            String distance    = doc != null && doc.distanceUnit != null  ? doc.distanceUnit: "—";
            String publishedAt = doc != null && doc.publishedAt != null   ? doc.publishedAt : "—";
            String permalink   = doc != null && doc.permalink != null     ? doc.permalink   : "—";
            String description = doc != null && doc.desc != null          ? doc.desc        : "—";

            String rawLicUrl = (doc != null && doc.licenses != null && !doc.licenses.isEmpty())
                    ? doc.licenses.get(0)
                    : null;
            String licKey = extractKeyFromUrl(rawLicUrl);
            LicenseEntity lic = licKey != null
                    ? db.licenseDao().getByKey(licKey)
                    : null;
            String licenseName = lic != null
                    ? lic.name
                    : (licKey != null ? licKey : "—");

            String rawGsUrl = doc != null ? doc.gamesystem : null;
            String gsKey = extractKeyFromUrl(rawGsUrl);
            GameSystemEntity gs = gsKey != null
                    ? db.gameSystemDao().getByKey(gsKey)
                    : null;
            String gsName = gs != null
                    ? gs.name
                    : (gsKey != null ? gsKey : "—");

            requireActivity().runOnUiThread(() -> {
                tvName      .setText(name);
                tvAuthor    .setText("Author: "      + author);
                tvLicense   .setText(licenseName);
                tvGameSystem.setText("Game System"   +gsName);
                tvDistance  .setText("Distance: "    + distance);
                tvPublished .setText("Published: "   + publishedAt);
                tvPermalink .setText(permalink);
                tvDesc      .setText(description);

                tvPermalink.setAutoLinkMask(Linkify.WEB_URLS);
                tvPermalink.setMovementMethod(LinkMovementMethod.getInstance());
            });
        });

        return dialog;
    }

    private @Nullable String extractKeyFromUrl(@Nullable String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        int idx = u.lastIndexOf('/');
        return idx != -1
                ? u.substring(idx + 1)
                : u;
    }
}
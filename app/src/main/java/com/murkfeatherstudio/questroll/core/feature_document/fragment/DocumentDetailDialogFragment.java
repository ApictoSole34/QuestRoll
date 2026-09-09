package com.murkfeatherstudio.questroll.core.feature_document.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.murkfeatherstudio.questroll.core.local_database.Open5eDatabase;
import com.murkfeatherstudio.questroll.core.models.open5e.document.DocumentEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.game_system.GameSystemEntity;
import com.murkfeatherstudio.questroll.core.models.open5e.license.LicenseEntity;
import com.murkfeatherstudio.questroll.databinding.FragmentDocumentDetailBinding;

import java.util.concurrent.Executors;

public class DocumentDetailDialogFragment extends DialogFragment {
    private static final String ARG_KEY = "key";
    private FragmentDocumentDetailBinding binding;

    public static DocumentDetailDialogFragment newInstance(String key) {
        DocumentDetailDialogFragment fragment = new DocumentDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String key = requireArguments().getString(ARG_KEY);
        binding = FragmentDocumentDetailBinding.inflate(LayoutInflater.from(getContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(binding.getRoot())
                .setPositiveButton("Close", null)
                .create();

        /**
         * JAVADOC: We use runOnUiThread to update the binding fields after fetching 
         * data from the database in a background thread. View Binding provides 
         * type-safe access to all document fields (author, license, system, etc.) 
         * defined in fragment_document_detail.xml.
         */
        Executors.newSingleThreadExecutor().execute(() -> {
            Open5eDatabase db = Open5eDatabase.getInstance(requireContext());
            DocumentEntity doc = db.documentDao().getByKey(key);

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

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.tvDocName.setText(name);
                    binding.tvDocAuthor.setText("Author: " + author);
                    binding.tvDocLicense.setText(licenseName);
                    binding.tvDocGamesystem.setText("Game System: " + gsName);
                    binding.tvDocDistance.setText("Distance: " + distance);
                    binding.tvDocPublished.setText("Published: " + publishedAt);
                    binding.tvDocPermalink.setText(permalink);
                    binding.tvDocDesc.setText(description);

                    binding.tvDocPermalink.setAutoLinkMask(Linkify.WEB_URLS);
                    binding.tvDocPermalink.setMovementMethod(LinkMovementMethod.getInstance());
                });
            }
        });

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private @Nullable String extractKeyFromUrl(@Nullable String url) {
        if (url == null) return null;
        String u = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String[] parts = u.split("/");
        if (parts.length > 0) return parts[parts.length - 1];
        return null;
    }
}

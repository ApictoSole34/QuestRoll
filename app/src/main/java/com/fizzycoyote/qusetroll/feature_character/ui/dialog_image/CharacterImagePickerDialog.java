package com.fizzycoyote.qusetroll.feature_character.ui.dialog_image;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.adapter.CharacterImagePickerDialogAdapter;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class CharacterImagePickerDialog extends DialogFragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UCROP_REQUEST_CODE = 2;
    private CharacterImagePickerDialogAdapter adapter;
    private List<String> imageList;
    private CharacterImagePickerDialogAdapter.OnImageClickListener listener;
    private String lastOriginalPath;


    public CharacterImagePickerDialog(CharacterImagePickerDialogAdapter.OnImageClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_character_image_picker, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        imageList = getImageListFromInternalStorage();
        imageList.addAll(getImageListFromAssets());

        adapter = new CharacterImagePickerDialogAdapter(imageList, new CharacterImagePickerDialogAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(String imagePath) {
                Toast.makeText(getContext(), "Image set", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onImageClick(imagePath);
                }
                dismiss();
            }

            @Override
            public void onAddImageClick() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        recyclerView.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Choose character avatar")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCROP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null && lastOriginalPath != null) {
                    try {

                        InputStream is = requireContext().getContentResolver().openInputStream(resultUri);
                        if (is != null) {
                            is.close();
                        }

                        if (listener != null) {
                            listener.onImageClick(lastOriginalPath);
                        }

                        refreshImageList();
                        dismiss();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                Toast.makeText(requireContext(), "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String imagePath = copyImageToInternalStorage(uri);
                if (imagePath != null) {
                    lastOriginalPath = imagePath;
                    File sourceFile = new File(imagePath);
                    Uri sourceUri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            sourceFile
                    );
                    startUCropActivity(sourceUri);
                }
            }
        }
    }

    private void startUCropActivity(Uri sourceUri) {
        try {
            File miniaturesDir = new File(requireContext().getFilesDir(), "characters_miniatures");
            if (!miniaturesDir.exists()) miniaturesDir.mkdirs();

            String originalName = new File(lastOriginalPath).getName();
            String miniatureName = originalName
                    .replace(".jpg", "_mini.jpg")
                    .replace(".png", "_mini.png");
            File destinationFile = new File(miniaturesDir, miniatureName);

            Uri destinationUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getApplicationContext().getPackageName() + ".fileprovider",
                    destinationFile
            );

            requireContext().grantUriPermission(
                    "com.yalantis.ucrop",
                    destinationUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(90);
            options.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            options.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));

            UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(1000, 1000)
                    .start(requireContext(), this, UCROP_REQUEST_CODE);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error starting image editor", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshImageList() {
        imageList.clear();
        imageList.addAll(getImageListFromInternalStorage());
        imageList.addAll(getImageListFromAssets());
        adapter.notifyDataSetChanged();
    }

    private String copyImageToInternalStorage(Uri uri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String filePath = null;

        try {
            File internalStorageDir = new File(requireContext().getFilesDir(), "characters_images");
            if (!internalStorageDir.exists()) {
                internalStorageDir.mkdir();
            }

            inputStream = requireContext().getContentResolver().openInputStream(uri);

            String mimeType = requireContext().getContentResolver().getType(uri);
            String extension = ".jpg";
            if (mimeType != null) {
                switch (mimeType) {
                    case "image/png":
                        extension = ".png";
                        break;
                    case "image/jpeg":
                        extension = ".jpg";
                        break;
                }
            }

            File imageFile = new File(internalStorageDir, "image_" + System.currentTimeMillis() + extension);
            filePath = imageFile.getAbsolutePath();

            outputStream = Files.newOutputStream(imageFile.toPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }

    private List<String> getImageListFromInternalStorage() {
        List<String> imageList = new ArrayList<>();
        File internalStorageDir = new File(requireContext().getFilesDir(), "characters_images");
        if (internalStorageDir.exists() && internalStorageDir.isDirectory()) {
            File[] files = internalStorageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (file.getName().endsWith(".png") || file.getName().endsWith(".jpg"))) {
                        imageList.add(file.getAbsolutePath());
                    }
                }
            }
        }
        return imageList;
    }

    private List<String> getImageListFromAssets() {
        List<String> imageList = new ArrayList<>();
        try {
            String[] files = requireContext().getAssets().list("characters_images");
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".png") || file.endsWith(".jpg")) {
                        imageList.add("assets://characters_images/" + file);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageList;
    }
}
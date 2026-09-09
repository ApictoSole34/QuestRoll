package com.murkfeatherstudio.questroll.feature_character.ui.wizard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.murkfeatherstudio.questroll.R;
import com.murkfeatherstudio.questroll.databinding.FragmentWizardImageBinding;
import com.murkfeatherstudio.questroll.feature_character.view_model.WizardViewModel;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageStepFragment extends Fragment {

    private static final String ASSETS_IMAGES_DIR     = "characters_images";
    private static final String ASSETS_THUMBNAILS_DIR = "characters_miniatures";
    private static final String DEFAULT_IMAGE_ASSET = "ai-generated-9221232_1920.png";
    private static final String DEFAULT_THUMB_ASSET = "ai-generated-9221232_1920_mini.png";

    private WizardViewModel viewModel;
    private FragmentWizardImageBinding binding;

    private boolean waitingForThumbnail = false;
    private boolean waitingForFullImage  = false;

    private File fullImagesDir;
    private File thumbnailsDir;
    private File tempDir;

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    if (waitingForThumbnail)      startCrop(uri, true);
                    else if (waitingForFullImage) startCrop(uri, false);
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) openGallery();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWizardImageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        fullImagesDir = new File(requireContext().getFilesDir(), "characters_images");
        thumbnailsDir = new File(requireContext().getFilesDir(), "characters_miniatures");
        tempDir       = new File(requireContext().getCacheDir(), "ucrop_temp");
        ensureDir(fullImagesDir);
        ensureDir(thumbnailsDir);
        ensureDir(tempDir);

        loadOrCopyDefaultImages();

        binding.selectThumbnailButton.setOnClickListener(v -> {
            waitingForThumbnail = true;
            waitingForFullImage  = false;
            checkPermissionAndOpenGallery();
        });

        binding.selectFullImageButton.setOnClickListener(v -> {
            waitingForFullImage  = true;
            waitingForThumbnail = false;
            checkPermissionAndOpenGallery();
        });

        binding.selectFromAssetsButton.setOnClickListener(v -> showAssetImagePickerDialog());

        binding.skipButton.setOnClickListener(v -> {
            copyDefaultAssetsToPrivateStorage();
            showDefaultImagesFromFiles();
        });

        binding.nextButton.setOnClickListener(v -> goToNext());
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadOrCopyDefaultImages() {
        if (binding == null) return;
        boolean hasThumb = viewModel.characterThumbnailPath != null && new File(viewModel.characterThumbnailPath).exists();
        boolean hasFull   = viewModel.characterImagePath != null && new File(viewModel.characterImagePath).exists();

        if (hasThumb && hasFull) {
            binding.thumbnailPreview.setImageURI(Uri.fromFile(new File(viewModel.characterThumbnailPath)));
            binding.fullImagePreview.setImageURI(Uri.fromFile(new File(viewModel.characterImagePath)));
        } else {
            copyDefaultAssetsToPrivateStorage();
            showDefaultImagesFromFiles();
        }
    }

    private void copyDefaultAssetsToPrivateStorage() {
        File defaultFull = new File(fullImagesDir, "default_full.png");
        if (!defaultFull.exists()) copyAssetToFile(ASSETS_IMAGES_DIR + "/" + DEFAULT_IMAGE_ASSET, defaultFull);
        viewModel.characterImagePath = defaultFull.getAbsolutePath();

        File defaultThumb = new File(thumbnailsDir, "default_thumb.png");
        if (!defaultThumb.exists()) copyAssetToFile(ASSETS_THUMBNAILS_DIR + "/" + DEFAULT_THUMB_ASSET, defaultThumb);
        viewModel.characterThumbnailPath = defaultThumb.getAbsolutePath();
    }

    private void showDefaultImagesFromFiles() {
        if (binding == null) return;
        if (viewModel.characterImagePath != null) binding.fullImagePreview.setImageURI(Uri.fromFile(new File(viewModel.characterImagePath)));
        if (viewModel.characterThumbnailPath != null) binding.thumbnailPreview.setImageURI(Uri.fromFile(new File(viewModel.characterThumbnailPath)));
    }

    private void showAssetImagePickerDialog() {
        List<String> imageFiles = new ArrayList<>();
        try {
            String[] files = requireContext().getAssets().list(ASSETS_IMAGES_DIR);
            if (files != null) {
                for (String f : files) if (!f.contains("_mini")) imageFiles.add(f);
            }
        } catch (IOException e) { Log.e("ImageStep", "Error listing assets", e); }

        GridView gridView = new GridView(requireContext());
        gridView.setNumColumns(3);
        gridView.setAdapter(new AssetThumbnailAdapter(imageFiles));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose image")
                .setView(gridView)
                .setNegativeButton("Cancel", null)
                .create();

        gridView.setOnItemClickListener((parent, v, position, id) -> {
            dialog.dismiss();
            applyAssetImages(imageFiles.get(position));
        });
        dialog.show();
    }

    private class AssetThumbnailAdapter extends BaseAdapter {
        private final List<String> imageFiles;
        AssetThumbnailAdapter(List<String> imageFiles) { this.imageFiles = imageFiles; }
        @Override public int getCount() { return imageFiles.size(); }
        @Override public Object getItem(int pos) { return imageFiles.get(pos); }
        @Override public long getItemId(int pos) { return pos; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            ImageView img = (convertView instanceof ImageView) ? (ImageView) convertView : new ImageView(requireContext());
            img.setLayoutParams(new GridView.LayoutParams(dpToPx(100), dpToPx(100)));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String fullName = imageFiles.get(position);
            try (InputStream is = requireContext().getAssets().open(ASSETS_THUMBNAILS_DIR + "/" + buildThumbnailName(fullName))) {
                img.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (IOException e) { img.setImageResource(android.R.drawable.ic_menu_gallery); }
            return img;
        }
    }

    private void applyAssetImages(String imageFileName) {
        if (binding == null) return;
        File destImage = new File(fullImagesDir, UUID.randomUUID() + "_" + imageFileName);
        File destThumb = new File(thumbnailsDir, UUID.randomUUID() + "_" + buildThumbnailName(imageFileName));

        if (copyAssetToFile(ASSETS_IMAGES_DIR + "/" + imageFileName, destImage)) {
            viewModel.characterImagePath = destImage.getAbsolutePath();
            binding.fullImagePreview.setImageURI(Uri.fromFile(destImage));
        }
        if (copyAssetToFile(ASSETS_THUMBNAILS_DIR + "/" + buildThumbnailName(imageFileName), destThumb)) {
            viewModel.characterThumbnailPath = destThumb.getAbsolutePath();
            binding.thumbnailPreview.setImageURI(Uri.fromFile(destThumb));
        }
    }

    private String buildThumbnailName(String imageFileName) {
        int dot = imageFileName.lastIndexOf('.');
        if (dot > 0) return imageFileName.substring(0, dot) + "_mini" + imageFileName.substring(dot);
        return imageFileName + "_mini";
    }

    private boolean copyAssetToFile(String assetPath, File destFile) {
        try (InputStream in = requireContext().getAssets().open(assetPath); OutputStream out = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return true;
        } catch (IOException e) { return false; }
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else openGallery();
        } else openGallery();
    }

    private void openGallery() { galleryLauncher.launch("image/*"); }

    private void startCrop(Uri sourceUri, boolean isThumbnail) {
        File tempFile = new File(tempDir, UUID.randomUUID() + (isThumbnail ? "_thumb_temp.jpg" : "_full_temp.jpg"));
        UCrop uCrop = UCrop.of(sourceUri, Uri.fromFile(tempFile));
        if (isThumbnail) uCrop = uCrop.withAspectRatio(1, 1).withMaxResultSize(512, 512);
        else uCrop = uCrop.withMaxResultSize(1024, 1024);
        uCrop.start(requireContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                File tempFile = new File(resultUri.getPath());
                if (waitingForThumbnail) {
                    File dest = new File(thumbnailsDir, UUID.randomUUID() + "_mini.jpg");
                    if (copyFile(tempFile, dest)) {
                        viewModel.characterThumbnailPath = dest.getAbsolutePath();
                        if (binding != null) binding.thumbnailPreview.setImageURI(Uri.fromFile(dest));
                    }
                } else if (waitingForFullImage) {
                    File dest = new File(fullImagesDir, UUID.randomUUID() + ".jpg");
                    if (copyFile(tempFile, dest)) {
                        viewModel.characterImagePath = dest.getAbsolutePath();
                        if (binding != null) binding.fullImagePreview.setImageURI(Uri.fromFile(dest));
                        // BUG FIX: Automatically create thumbnail from full image if not set
                        createThumbnailFromFullImage(dest);
                    }
                }
                tempFile.delete();
            }
        }
        waitingForThumbnail = false;
        waitingForFullImage = false;
    }

    private void createThumbnailFromFullImage(File fullImageFile) {
        try {
            Bitmap fullBitmap = BitmapFactory.decodeFile(fullImageFile.getAbsolutePath());
            if (fullBitmap == null) return;
            Bitmap thumbBitmap = Bitmap.createScaledBitmap(fullBitmap, 256, 256, true);
            File thumbFile = new File(thumbnailsDir, UUID.randomUUID() + "_auto_mini.jpg");
            try (FileOutputStream out = new FileOutputStream(thumbFile)) {
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                viewModel.characterThumbnailPath = thumbFile.getAbsolutePath();
                if (binding != null) binding.thumbnailPreview.setImageURI(Uri.fromFile(thumbFile));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean copyFile(File src, File dst) {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return true;
        } catch (IOException e) { return false; }
    }

    private void ensureDir(File dir) { if (!dir.exists()) dir.mkdirs(); }
    private int dpToPx(int dp) { return Math.round(dp * requireContext().getResources().getDisplayMetrics().density); }
    private void goToNext() { Navigation.findNavController(requireView()).navigate(R.id.next_action); }
}

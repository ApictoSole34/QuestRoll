package com.fizzycoyote.qusetroll.feature_character.ui.wizard;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.utils.ImageUtils;
import com.fizzycoyote.qusetroll.feature_character.view_model.WizardViewModel;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment for selecting character image.
 * Default graphics are copied from assets and stored in private directory,
 * so their paths are permanently saved in ViewModel and stored in the database.
 */
public class ImageStepFragment extends Fragment {

    private static final String ASSETS_IMAGES_DIR     = "characters_images";
    private static final String ASSETS_THUMBNAILS_DIR = "characters_miniatures";
    private static final String DEFAULT_IMAGE_ASSET = "ai-generated-9221232_1920.png";
    private static final String DEFAULT_THUMB_ASSET = "ai-generated-9221232_1920_mini.png";

    private WizardViewModel viewModel;
    private CircleImageView thumbnailPreview;
    private ImageView fullImagePreview;
    private Button selectThumbnailButton, selectFullImageButton,
            selectFromAssetsButton, skipButton, nextButton, backButton;

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
                } else {
                    waitingForThumbnail = false;
                    waitingForFullImage  = false;
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    waitingForThumbnail = false;
                    waitingForFullImage  = false;
                    Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wizard_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WizardViewModel.class);

        fullImagesDir = new File(requireContext().getFilesDir(), "characters_images");
        thumbnailsDir = new File(requireContext().getFilesDir(), "characters_miniatures");
        tempDir       = new File(requireContext().getCacheDir(), "ucrop_temp");
        ensureDir(fullImagesDir);
        ensureDir(thumbnailsDir);
        ensureDir(tempDir);

        thumbnailPreview       = view.findViewById(R.id.thumbnail_preview);
        fullImagePreview       = view.findViewById(R.id.full_image_preview);
        selectThumbnailButton  = view.findViewById(R.id.select_thumbnail_button);
        selectFullImageButton  = view.findViewById(R.id.select_full_image_button);
        selectFromAssetsButton = view.findViewById(R.id.select_from_assets_button);
        skipButton             = view.findViewById(R.id.skip_button);
        nextButton             = view.findViewById(R.id.next_button);
        backButton             = view.findViewById(R.id.back_button);

        loadOrCopyDefaultImages();

        selectThumbnailButton.setOnClickListener(v -> {
            waitingForThumbnail = true;
            waitingForFullImage  = false;
            checkPermissionAndOpenGallery();
        });

        selectFullImageButton.setOnClickListener(v -> {
            waitingForFullImage  = true;
            waitingForThumbnail = false;
            checkPermissionAndOpenGallery();
        });

        selectFromAssetsButton.setOnClickListener(v -> showAssetImagePickerDialog());

        skipButton.setOnClickListener(v -> {
            copyDefaultAssetsToPrivateStorage();
            showDefaultImagesFromFiles();
            Toast.makeText(getContext(), "Default images restored", Toast.LENGTH_SHORT).show();
        });

        nextButton.setOnClickListener(v -> goToNext());
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.back_action));
    }

    private void loadOrCopyDefaultImages() {
        boolean hasThumb = viewModel.characterThumbnailPath != null && new File(viewModel.characterThumbnailPath).exists();
        boolean hasFull   = viewModel.characterImagePath != null && new File(viewModel.characterImagePath).exists();

        if (hasThumb && hasFull) {
            thumbnailPreview.setImageURI(Uri.fromFile(new File(viewModel.characterThumbnailPath)));
            fullImagePreview.setImageURI(Uri.fromFile(new File(viewModel.characterImagePath)));
        } else {
            copyDefaultAssetsToPrivateStorage();
            showDefaultImagesFromFiles();
        }
    }

    private void copyDefaultAssetsToPrivateStorage() {
        File defaultFull = new File(fullImagesDir, "default_full.png");
        if (!defaultFull.exists()) {
            copyAssetToFile(ASSETS_IMAGES_DIR + "/" + DEFAULT_IMAGE_ASSET, defaultFull);
        }
        viewModel.characterImagePath = defaultFull.getAbsolutePath();

        File defaultThumb = new File(thumbnailsDir, "default_thumb.png");
        if (!defaultThumb.exists()) {
            copyAssetToFile(ASSETS_THUMBNAILS_DIR + "/" + DEFAULT_THUMB_ASSET, defaultThumb);
        }
        viewModel.characterThumbnailPath = defaultThumb.getAbsolutePath();
    }

    private void showDefaultImagesFromFiles() {
        File fullFile = new File(viewModel.characterImagePath);
        File thumbFile = new File(viewModel.characterThumbnailPath);
        if (fullFile.exists()) fullImagePreview.setImageURI(Uri.fromFile(fullFile));
        if (thumbFile.exists()) thumbnailPreview.setImageURI(Uri.fromFile(thumbFile));
    }

    private void showAssetImagePickerDialog() {
        List<String> imageFiles = new ArrayList<>();
        try {
            String[] files = requireContext().getAssets().list(ASSETS_IMAGES_DIR);
            if (files != null) {
                for (String f : files) {
                    if (!f.contains("_mini")) imageFiles.add(f);
                }
            }
        } catch (IOException e) {
            Log.e("ImageStep", "Error listing assets", e);
        }

        if (imageFiles.isEmpty()) {
            Toast.makeText(getContext(), "No images in asset gallery", Toast.LENGTH_SHORT).show();
            return;
        }

        GridView gridView = new GridView(requireContext());
        gridView.setNumColumns(3);
        int padding = dpToPx(8);
        gridView.setPadding(padding, padding, padding, padding);
        gridView.setHorizontalSpacing(dpToPx(6));
        gridView.setVerticalSpacing(dpToPx(6));

        AssetThumbnailAdapter adapter = new AssetThumbnailAdapter(imageFiles);
        gridView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose character image")
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
        private final int cellSize = dpToPx(100);

        AssetThumbnailAdapter(List<String> imageFiles) { this.imageFiles = imageFiles; }
        @Override public int getCount()              { return imageFiles.size(); }
        @Override public Object getItem(int pos)     { return imageFiles.get(pos); }
        @Override public long getItemId(int pos)     { return pos; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView img;
            if (convertView instanceof ImageView) {
                img = (ImageView) convertView;
            } else {
                img = new ImageView(requireContext());
                img.setLayoutParams(new GridView.LayoutParams(cellSize, cellSize));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setPadding(2, 2, 2, 2);
            }

            String fullName  = imageFiles.get(position);
            String thumbName = buildThumbnailName(fullName);
            String thumbPath = ASSETS_THUMBNAILS_DIR + "/" + thumbName;

            try (InputStream is = requireContext().getAssets().open(thumbPath)) {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                img.setImageBitmap(bmp);
            } catch (IOException e) {
                try (InputStream is = requireContext().getAssets().open(ASSETS_IMAGES_DIR + "/" + fullName)) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    img.setImageBitmap(bmp);
                } catch (IOException ex) {
                    img.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            return img;
        }
    }

    private void applyAssetImages(String imageFileName) {
        String thumbFileName = buildThumbnailName(imageFileName);
        File destImage = new File(fullImagesDir, UUID.randomUUID() + "_" + imageFileName);
        File destThumb = new File(thumbnailsDir,  UUID.randomUUID() + "_" + thumbFileName);

        boolean imageCopied = copyAssetToFile(ASSETS_IMAGES_DIR + "/" + imageFileName, destImage);
        boolean thumbCopied = copyAssetToFile(ASSETS_THUMBNAILS_DIR + "/" + thumbFileName, destThumb);

        if (imageCopied) {
            viewModel.characterImagePath = destImage.getAbsolutePath();
            fullImagePreview.setImageURI(Uri.fromFile(destImage));
        } else {
            Toast.makeText(getContext(), "Failed to copy image", Toast.LENGTH_SHORT).show();
        }

        if (thumbCopied) {
            viewModel.characterThumbnailPath = destThumb.getAbsolutePath();
            thumbnailPreview.setImageURI(Uri.fromFile(destThumb));
        } else {
            Toast.makeText(getContext(), "Thumbnail not found for selected image", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildThumbnailName(String imageFileName) {
        int dot = imageFileName.lastIndexOf('.');
        if (dot > 0) return imageFileName.substring(0, dot) + "_mini" + imageFileName.substring(dot);
        return imageFileName + "_mini";
    }

    private boolean copyAssetToFile(String assetPath, File destFile) {
        try (InputStream in = requireContext().getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return true;
        } catch (IOException e) {
            Log.w("ImageStep", "Asset copy error: " + assetPath, e);
            return false;
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() { galleryLauncher.launch("image/*"); }

    private void startCrop(Uri sourceUri, boolean isThumbnail) {
        File tempFile = new File(tempDir, UUID.randomUUID() + (isThumbnail ? "_thumb_temp.jpg" : "_full_temp.jpg"));
        Uri destinationUri = Uri.fromFile(tempFile);

        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        if (isThumbnail) uCrop = uCrop.withAspectRatio(1, 1).withMaxResultSize(512, 512);
        else uCrop = uCrop.withMaxResultSize(1024, 1024);
        uCrop.withOptions(getUcropOptions()).start(requireContext(), this);
    }

    private UCrop.Options getUcropOptions() {
        UCrop.Options o = new UCrop.Options();
        o.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        o.setCompressionQuality(90);
        o.setFreeStyleCropEnabled(true);
        o.setHideBottomControls(false);
        o.setToolbarTitle("Crop image");
        return o;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null && "file".equals(resultUri.getScheme())) {
                File tempFile = new File(resultUri.getPath());
                if (tempFile.exists()) {
                    if (waitingForThumbnail) {
                        File dest = new File(thumbnailsDir, UUID.randomUUID() + "_mini.jpg");
                        if (copyFile(tempFile, dest)) {
                            viewModel.characterThumbnailPath = dest.getAbsolutePath();
                            thumbnailPreview.setImageURI(Uri.fromFile(dest));
                            Toast.makeText(getContext(), "Thumbnail saved", Toast.LENGTH_SHORT).show();
                        }
                    } else if (waitingForFullImage) {
                        File dest = new File(fullImagesDir, UUID.randomUUID() + ".jpg");
                        if (copyFile(tempFile, dest)) {
                            viewModel.characterImagePath = dest.getAbsolutePath();
                            fullImagePreview.setImageURI(Uri.fromFile(dest));
                            Toast.makeText(getContext(), "Full image saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                    tempFile.delete();
                } else {
                    Toast.makeText(getContext(), "Temporary file not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Invalid result URI", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), "Crop cancelled", Toast.LENGTH_SHORT).show();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Log.e("ImageStep", "UCrop error", UCrop.getError(data));
            Toast.makeText(getContext(), "Crop error", Toast.LENGTH_SHORT).show();
        }
        waitingForThumbnail = false;
        waitingForFullImage = false;
    }

    private boolean copyFile(File src, File dst) {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void ensureDir(File dir) { if (!dir.exists()) dir.mkdirs(); }
    private int dpToPx(int dp) {
        return Math.round(dp * requireContext().getResources().getDisplayMetrics().density);
    }
    private void goToNext() { Navigation.findNavController(requireView()).navigate(R.id.next_action); }
}
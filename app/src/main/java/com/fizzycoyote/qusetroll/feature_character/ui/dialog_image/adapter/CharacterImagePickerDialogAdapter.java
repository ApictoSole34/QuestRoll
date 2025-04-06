package com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.fizzycoyote.qusetroll.R;
import com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.helper.CharacterImageHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CharacterImagePickerDialogAdapter extends RecyclerView.Adapter<CharacterImagePickerDialogAdapter.ImageViewHolder> {
    private List<String> imageList;
    private OnImageClickListener listener;

    public CharacterImagePickerDialogAdapter(List<String> imageList, OnImageClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_character_dialog_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.itemView.invalidate();

        if (position == 0) {
            holder.bindAddButton();
        } else {
            String imagePath = imageList.get(position - 1);
            holder.bind(imagePath);
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size() + 1;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        private Bitmap loadBitmap(String path) throws IOException {
            if (path.startsWith("assets://")) {
                InputStream is = itemView.getContext().getAssets().open(path.replace("assets://", ""));
                return BitmapFactory.decodeStream(is);
            }
            return BitmapFactory.decodeFile(path);
        }

        public void bind(String imagePath) {
            try {
                Bitmap bitmap = loadBitmap(imagePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(imagePath);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if(imagePath.startsWith("assets://")) {
                    Toast.makeText(itemView.getContext(), "his image is from the app and cannot be deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    showDeleteConfirmationDialog(imagePath);
                }
                return true;
            });
        }

        private void showDeleteConfirmationDialog(String imagePath) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteImage(imagePath);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }

        private void deleteImage(String imagePath) {
            File file = new File(imagePath);
            File miniatureFile = new File(CharacterImageHelper.getMiniaturePathForImage(imagePath));
            if (file.delete() && miniatureFile.exists()) {
                imageList.remove(imagePath);
                miniatureFile.delete();
                notifyDataSetChanged();
                Toast.makeText(itemView.getContext(), "Image deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(itemView.getContext(), "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }

        public void bindAddButton() {
            imageView.setImageResource(R.drawable.ic_add);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddImageClick();
                }
            });
        }
    }

    public interface OnImageClickListener {
        void onImageClick(String imagePath);
        void onAddImageClick();
    }
}

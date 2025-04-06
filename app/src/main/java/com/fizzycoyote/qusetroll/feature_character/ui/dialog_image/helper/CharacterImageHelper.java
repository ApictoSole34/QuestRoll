package com.fizzycoyote.qusetroll.feature_character.ui.dialog_image.helper;


import java.io.File;

public class CharacterImageHelper {
    public static String getMiniaturePathForImage(String imagePath) {
        if (imagePath == null) return null;

        try {
            if (imagePath.startsWith("assets://")) {
                String assetPath = imagePath.replace("assets://", "");
                int lastSlash = assetPath.lastIndexOf('/');
                if (lastSlash == -1) return null;

                String filename = assetPath.substring(lastSlash + 1);
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex == -1) return null;

                String base = filename.substring(0, dotIndex);
                String newFilename = base + "_mini.png";

                return "assets://characters_miniatures/" + newFilename;
            } else {
                File imageFile = new File(imagePath);
                String fileName = imageFile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex == -1) return null;

                String base = fileName.substring(0, dotIndex);
                String extension = fileName.substring(dotIndex);
                String miniatureName = base + "_mini" + extension;

                File miniaturesDir = new File(imageFile.getParentFile().getParentFile(), "characters_miniatures");
                if (!miniaturesDir.exists()) miniaturesDir.mkdirs();

                return new File(miniaturesDir, miniatureName).getAbsolutePath();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
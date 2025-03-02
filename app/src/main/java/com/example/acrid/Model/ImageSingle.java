package com.example.acrid.Model;

import android.util.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageSingle {
    private File image;

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = new File(encodeFileToBase64(image));
    }

    public ImageSingle(File image) {
        this.image = image;
    }
    private String encodeFileToBase64(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

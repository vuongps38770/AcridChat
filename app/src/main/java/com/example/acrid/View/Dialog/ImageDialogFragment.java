package com.example.acrid.View.Dialog;

import android.app.Dialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.acrid.R;
import com.github.chrisbanes.photoview.PhotoView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageDialogFragment extends DialogFragment {

    private String imageUrl; // URL của hình ảnh

    public ImageDialogFragment(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_dialog, container, false);

        PhotoView photoView = view.findViewById(R.id.photoView);


        ImageButton back = view.findViewById(R.id.btnBack);
        back.setOnClickListener(view1 -> {
            this.dismiss();
        });
        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.err)
                .into(photoView);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
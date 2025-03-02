package com.example.acrid.View.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.People;
import com.example.acrid.Model.User;
import com.example.acrid.R;
import com.example.acrid.utils.TimeUtils;
import com.makeramen.roundedimageview.RoundedImageView;

public class InfoDialog {

    private String userUID;
    private Context context;

    public InfoDialog(String userUID,Context context) {
        this.userUID = userUID;
        this.context =context;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.profile_dialog,null);
        ImageView btnBack = view.findViewById(R.id.btnBack);
        RoundedImageView avt = view.findViewById(R.id.avt);
        TextView name = view.findViewById(R.id.name);
        TextView idname = view.findViewById(R.id.idname);
        TextView status = view.findViewById(R.id.status);
        TextView birthDate = view.findViewById(R.id.birthDate);
        TextView createDate = view.findViewById(R.id.createđate);
        AlertDialog dialog = builder.create();
        UserRepo.getUserByUID(userUID).observeForever(user -> {
            status.setText(user.getStatus());
            idname.setText(user.getIDName());
            birthDate.setText(user.getBirthDay().isEmpty()?"../../...":user.getBirthDay());
            createDate.setText(TimeUtils.miliToString(user.getCreatedDate()));
            name.setText(user.getDisplayName());
            Glide.with(avt).load(user.getProfileIMG())
                    .placeholder(R.drawable.load)
                    .error(R.drawable.err)
                    .into(avt);
        });
        btnBack.setOnClickListener(view1 -> dialog.dismiss());
        dialog.setView(view);

        dialog.show();


    }
}

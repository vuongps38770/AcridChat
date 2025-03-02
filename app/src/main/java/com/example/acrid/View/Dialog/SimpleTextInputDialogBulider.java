package com.example.acrid.View.Dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.acrid.R;

public class SimpleTextInputDialogBulider {
    public interface DissmissListener{
        void onClicked(AlertDialog dialog);
    }
    public interface ApproveListener{
        void onClicked(AlertDialog dialog,String arg);
    }
    private String title="";
    private String content ="";
    private Context context;
    private DissmissListener dissmissListener;
    private ApproveListener approveListener;
    public SimpleTextInputDialogBulider(Context context){
        this.context = context;
    }

    public static SimpleTextInputDialogBulider create(Context context){
        return new SimpleTextInputDialogBulider(context);
    }
    public SimpleTextInputDialogBulider setContent(String content){
        this.content =content;
        return this;
    }

    public SimpleTextInputDialogBulider setTitle(String title) {
        this.title = title;
        return this;
    }

    public SimpleTextInputDialogBulider setDissmiss(DissmissListener dissmissListener) {
        this.dissmissListener=dissmissListener;
        return this;
    }
    public SimpleTextInputDialogBulider setApprove(ApproveListener approveListener) {
        this.approveListener=approveListener;
        return this;
    }
    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.simple_text_dialog,null);
        builder.setView(v);
        AlertDialog dialog = builder.create();
        TextView title = v.findViewById(R.id.title);
        title.setText(this.title);
        EditText content = v.findViewById(R.id.edt);
        content.setText(this.content);


        Button dissmiss = v.findViewById(R.id.btnDismiss);
        Button confirm = v.findViewById(R.id.btnConfirm);
        confirm.setOnClickListener(v1 -> {
            if(approveListener== null) return;
            approveListener.onClicked(dialog,content.getText().toString());
        });
        dissmiss.setOnClickListener(v1 -> {
            if(dissmissListener==null)return;
            dissmissListener.onClicked(dialog);
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

}

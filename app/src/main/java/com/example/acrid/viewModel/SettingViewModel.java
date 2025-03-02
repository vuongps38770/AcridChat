package com.example.acrid.viewModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.ChatRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.State.SimpleCallBack;
import com.example.acrid.View.Dialog.ImageDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SettingViewModel extends ViewModel {
    public MutableLiveData<String> avtUrl = new MutableLiveData<>("");
    public MutableLiveData<String> status = new MutableLiveData<>("");
    public MutableLiveData<String> name = new MutableLiveData<>("");
    public MutableLiveData<String> tempIMGurl = new MutableLiveData<>("");
    public MutableLiveData<Boolean> isOnLoading = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);
    public MutableLiveData<Bitmap> bitmapImg = new MutableLiveData<>();
    public MutableLiveData<Uri> uriImg = new MutableLiveData<>();
    public MutableLiveData<File> imgFile = new MutableLiveData<>();
    public MutableLiveData<String> errMess = new MutableLiveData<>("");


    public SettingViewModel() {
        UserRepo.getUserByUID(UserRepo.getCurrentUserUID()).observeForever(user -> {
            if(user!=null){
                avtUrl.postValue(user.getProfileIMG());
                status.postValue(user.getDescription().isEmpty()?"Không có mô tả":user.getDescription());
                name.postValue(user.getDisplayName());
            }
        });
    }
    public void setImgBitmap(Bitmap img,Context context){
        bitmapImg.postValue(img);
        imgFile.postValue(bitmapToFile(context,img));
    }
    public void setImgUri(Uri img, Context context){
        uriImg.postValue(img);
        try {
            imgFile.postValue(uriToFile(context,img));
        }catch (IOException e){
            Log.e("setImgUri: ",e.getMessage() );
        }

    }
    public void logout(){
        AuthRepo.logout();
        isLoggedOut.postValue(true);
    }


    public void saveImg(){
        if(imgFile==null) return;

        ChatRepo.sendImageSingle(imgFile.getValue(), new ChatRepo.SendImageCallBack() {
            @Override
            public void onSuccess(String imgUrl) {
                imgFile.postValue(null);
                isOnLoading.postValue(true);
                UserRepo.saveAVT(imgUrl, UserRepo.getCurrentUserUID(), new SimpleCallBack<>() {
                    @Override
                    public void onSucess(@Nullable String data) {
                        isOnLoading.postValue(false);
                        avtUrl.postValue(imgUrl);
                    }
                    @Override
                    public void onError(String message) {
                        isOnLoading.postValue(false);
                        errMess.postValue("Lỗi không thể lưu ảnh");
                    }
                });
            }

            @Override
            public void onLoading(boolean onLoad) {
                isOnLoading.postValue(onLoad);
            }

            @Override

            public void onFailure(String err) {
                isOnLoading.postValue(false);
                imgFile.postValue(null);
                errMess.postValue("Lỗi không thể lưu ảnh");


            }

            @Override
            public void onError() {
                isOnLoading.postValue(false);
                imgFile.postValue(null);
                errMess.postValue("Lỗi không thể lưu ảnh");


            }
        });
    }




    private File bitmapToFile(Context context, Bitmap bitmap) {
        File file = new File(context.getCacheDir(), "temp_image.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    private File uriToFile(Context context, Uri uri) throws IOException {
        File file = new File(context.getCacheDir(), "temp_image.jpg");
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }

    public void dismiss() {
        imgFile.postValue(null);
        avtUrl.setValue(avtUrl.getValue());
    }

    public void openImgDialog(FragmentManager manager) {
        ImageDialogFragment dialogFragment = new ImageDialogFragment(avtUrl.getValue());
        dialogFragment.show(manager, "image_dialog");
    }
}

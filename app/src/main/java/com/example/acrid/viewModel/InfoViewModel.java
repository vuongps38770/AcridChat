package com.example.acrid.viewModel;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.State.SimpleCallBack;
import com.example.acrid.View.Dialog.SimpleTextInputDialogBulider;
import com.example.acrid.utils.TimeUtils;

import java.util.Calendar;

public class InfoViewModel extends ViewModel {
    public MutableLiveData<String> name = new MutableLiveData<>("");
    public MutableLiveData<String> idName = new MutableLiveData<>("");
    public MutableLiveData<String> birthDate = new MutableLiveData<>("");
    public MutableLiveData<String> description = new MutableLiveData<>("");
    public MutableLiveData<String> email = new MutableLiveData<>("");
    public MutableLiveData<String> createdDate = new MutableLiveData<>("");
    public MutableLiveData<String> userUID = new MutableLiveData<>("");
    public MutableLiveData<String> errMess = new MutableLiveData<>("");

    public InfoViewModel() {
        userUID.setValue(UserRepo.getCurrentUserUID());
        userUID.observeForever(string -> {
            UserRepo.getUserByUID(string).observeForever(user -> {
                name.setValue(user.getDisplayName());
                idName.setValue(user.getIDName());
                birthDate.setValue(user.getBirthDay());
                description.setValue(user.getDescription());
                email.setValue(user.getEmail());
                createdDate.setValue(TimeUtils.miliToString(user.getCreatedDate()));
            });
        });
    }







    public void editName(Context context){
        SimpleTextInputDialogBulider.create(context)
                .setTitle("Đổi tên")
                .setContent(name.getValue())
                .setDissmiss(AppCompatDialog::dismiss)
                .setApprove((dialog, arg) -> {
                    if(arg.isEmpty()){
                        errMess.setValue("Không được bỏ trống");
                        return;
                    }
                    UserRepo.saveProperty(arg.trim(), DB.USER_COLLECTION.DISPLAY_NAME.toString()
                            , userUID.getValue(), new SimpleCallBack<String>() {
                                @Override
                                public void onSucess(@Nullable String data) {
                                    name.setValue(data);
                                    dialog.dismiss();
                                }

                                @Override
                                public void onError(String message) {
                                    errMess.setValue(message);
                                }
                            });
                })
                .show();

    }


    public void editDes(Context context){
        SimpleTextInputDialogBulider.create(context)
                .setTitle("Thêm chú thích")
                .setContent(description.getValue())
                .setDissmiss(AppCompatDialog::dismiss)
                .setApprove((dialog, arg) -> {
                    UserRepo.saveProperty(arg, DB.USER_COLLECTION.DESCRIPTION.toString()
                            , userUID.getValue(), new SimpleCallBack<String>() {
                                @Override
                                public void onSucess(@Nullable String data) {
                                    description.setValue(data);
                                    dialog.dismiss();
                                }

                                @Override
                                public void onError(String message) {
                                    errMess.setValue(message);
                                }
                            });
                })
                .show();

    }
















    public void openChoseDateDialog(Context context){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context);

        datePickerDialog.updateDate(year, month, day);

        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            DatePicker datePicker = datePickerDialog.getDatePicker();
            String selectedDate = String.format("%02d/%02d/%04d", datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
            UserRepo.saveProperty(selectedDate, DB.USER_COLLECTION.BIRTH_DATE.toString()
                    , userUID.getValue(), new SimpleCallBack<String>() {
                        @Override
                        public void onSucess(@Nullable String data) {
                            birthDate.setValue(data);
                        }

                        @Override
                        public void onError(String message) {
                            errMess.setValue(message);
                        }
                    });
        });

        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Hủy", (dialog, which) -> {
            dialog.dismiss();
        });
        datePickerDialog.show();
    }
}

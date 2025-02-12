package com.example.acrid.Helper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.acrid.Constant.Const;
import com.example.acrid.Constant.DB;
import com.example.acrid.Model.User;
import com.example.acrid.State.LoginError;
import com.example.acrid.State.LoginState;
import com.example.acrid.State.SignUpErorr;
import com.example.acrid.State.SignUpState;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthRepo {
    private static FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static void saveUser(@NonNull User user, @NonNull String password, @NonNull MutableLiveData<SignUpState> signUpStateLiveData) {
        if(password!=null&&!user.getEmail().isEmpty()){
            Log.e( "saveUser: ",password );
            Log.e( "saveUser: ",user.getEmail() );
        }else {
            Log.e( "saveUser: ","null" );
            return;
        }
        signUpStateLiveData.setValue(SignUpState.Loading.INSTANCE);
        List<SignUpErorr> errors = new ArrayList<>();
        //hàm tạo user với email và password
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.setUserUID(mAuth.getCurrentUser().getUid());
                        user.setCreatedDate(System.currentTimeMillis());
                        user.setProfileIMG(Optional.ofNullable(user.getProfileIMG()).orElse(Const.IMAGE_HOLDER_URL));
                        //lưu thêm thông tin user vào firestore
                        mFirestore.collection(DB.USER_COLLECTION.NAME.toString())
                                .document(user.getUserUID())
                                .set(user)
                                .addOnCompleteListener(
                                        task1 -> {
                                            if(task1.isSuccessful()) {
                                                Log.e("saveUser: ", "success");
                                                signUpStateLiveData.setValue(SignUpState.Success.INSTANCE);
                                            }else {
                                                Exception exception = task1.getException();
                                                Log.e("saveUser:addOnCompleteListener ", exception.toString());
                                                errors.add(SignUpErorr.OTHER);
                                                signUpStateLiveData.setValue(new SignUpState.Error(new ArrayList<>(errors)));
                                            }
                                        }
                                )
                                .addOnFailureListener(
                                        //bắt lỗi cho cái lưu vào firestore
                                        e -> {
                                            errors.add(SignUpErorr.OTHER);
                                            Log.e("saveUser:addOnFailureListener ", e.toString());
                                            signUpStateLiveData.setValue(new SignUpState.Error(new ArrayList<>(errors)));
                                        }
                                );
                    } else {
                        //bắt lỗi cho cái lưu vào firebase auth
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException) {
                            errors.add(SignUpErorr.EMAIL_ALREADY_EXISTS);
                        }
                        else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errors.add(SignUpErorr.EMAIL_INVALID);
                        }else if (exception instanceof FirebaseAuthException) {
                            errors.add(SignUpErorr.OTHER);
                            Log.e("FirebaseAuthException", exception.toString());
                        } else {
                            errors.add(SignUpErorr.OTHER);
                            Log.e("OtherException", exception.toString());
                        }
                        signUpStateLiveData.setValue(new SignUpState.Error(new ArrayList<>(errors)));
                    }
                })
                .addOnFailureListener(exception -> {
                    if (exception instanceof FirebaseAuthUserCollisionException) {
                        errors.add(SignUpErorr.EMAIL_ALREADY_EXISTS);
                    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                        errors.add(SignUpErorr.EMAIL_INVALID);
                    }else if (exception instanceof FirebaseAuthException) {
                        errors.add(SignUpErorr.OTHER);
                        Log.e("FirebaseAuthException2", exception.toString());
                    } else {
                        errors.add(SignUpErorr.OTHER);
                        Log.e("OtherException2", exception.toString());
                    }
                    signUpStateLiveData.setValue(new SignUpState.Error(new ArrayList<>(errors)));
                });
    }

    public static void loginWithEmailAndPassword(String email, String password, @NonNull MutableLiveData<LoginState> loginState) {
        loginState.setValue(LoginState.Loading.INSTANCE);
        List<LoginError> errors = new ArrayList<>();
        if (email.isEmpty()) {
            errors.add(LoginError.EMPTY_EMAIL);
        }
        if (password.isEmpty()) {
            errors.add(LoginError.EMPTY_PASSWORD);
        }
        if (!errors.isEmpty()) {
            loginState.setValue(new LoginState.Error(errors));
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginState.setValue(LoginState.Success.INSTANCE);
                    } else {
                        Exception exception = task.getException();

                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            errors.add(LoginError.UNREGISTERED_EMAIL);  // Email chưa đăng ký
                        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                            errors.add(LoginError.INVALID_CREDENTIALS); // Sai mật khẩu hoặc email sai định dạng
                        } else if (exception instanceof FirebaseAuthException) {
                            errors.add(LoginError.OTHER_ERROR);
                            Log.e("FirebaseAuthException", exception.toString());
                        } else {
                            errors.add(LoginError.OTHER_ERROR);
                            Log.e("OtherException", exception.toString());
                        }
                        Log.e("loginWithEmailAndPassword: ", exception.toString());
                        Log.e("loginWithEmailAndPassword: ", errors.size() + "");

                        loginState.setValue(new LoginState.Error(new ArrayList<>(errors)));
                    }
                })
                .addOnFailureListener(exception -> {
                    if (exception instanceof FirebaseAuthInvalidUserException) {
                        errors.add(LoginError.UNREGISTERED_EMAIL);
                    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                        errors.add(LoginError.INVALID_CREDENTIALS);
                    } else if (exception instanceof FirebaseAuthException) {
                        errors.add(LoginError.OTHER_ERROR);
                        Log.e("FirebaseAuthException2", exception.toString());
                    } else {
                        errors.add(LoginError.OTHER_ERROR);
                        Log.e("OtherException2", exception.toString());
                    }
                    loginState.setValue(new LoginState.Error(new ArrayList<>(errors)));
                });
    }
    public static void loginWithGoogleAccount(){

    }
    public static void logout() {
        mAuth.signOut();
    }

    public static void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email);
    }

    public static void updatePassword(String password) {
        mAuth.getCurrentUser().updatePassword(password);
    }
}

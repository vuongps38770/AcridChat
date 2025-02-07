package com.example.acrid.Model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.acrid.Constant.DB;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Auth extends ViewModel {

    public Auth() {

    }
    static {
        mAuth = FirebaseAuth.getInstance();
    }
    public static LiveData<User> currentUser;
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore db;
    static void Signin(String email, String PW){

    }
    static Task<Void> SignUp(User user){
        Map<String,Object> userData = new HashMap<>();
        userData.put("uid", user.getUserUID());
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("profileIMG", user.getProfileIMG());
        userData.put("birthDay", user.getBirthDay());
        userData.put("createdDate", com.google.firebase.Timestamp.now());
        return db.collection(DB.USER_COLLECTION.NAME.name()).document(user.getUserUID()).set(userData);
    }
}

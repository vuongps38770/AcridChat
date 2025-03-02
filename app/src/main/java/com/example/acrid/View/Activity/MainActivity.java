package com.example.acrid.View.Activity;

import static com.example.acrid.Helper.FireBaseService.CHANNEL_ID;
import static com.example.acrid.Helper.FireBaseService.NOTIFICATION_NAME;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Trace;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.acrid.BuildConfig;
import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.R;
import com.example.acrid.View.Fragment.LoginFragment;
import com.example.acrid.databinding.ActivityMainBinding;
import com.example.acrid.services.ChatHeadService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    DatabaseReference userRef;
    private final Set<Integer> hiddenBottomBarFrag= new HashSet<>(Arrays.asList(
            R.id.signUpFragment,
            R.id.loginFragment,
            R.id.chatFragment
    ));
    private MutableLiveData<String> userUID = new MutableLiveData<>("");
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
//                NotificationChannel channel = new NotificationChannel(
//                        CHANNEL_ID,
//                        NOTIFICATION_NAME,
//                        NotificationManager.IMPORTANCE_HIGH
//                );
//                channel.setDescription("Nhận thông báo tin nhắn từ bạn bè");
//
//                notificationManager.createNotificationChannel(channel);
//            }
//
//        }
        userUID.setValue(UserRepo.getCurrentUserUID());
        ////init trang thái online

        binding.getRoot().post(()->{
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            binding.bottom.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                int currentDestId = navController.getCurrentDestination().getId();

                if (itemId==R.id.chat && currentDestId != R.id.homeFragment) {
                    navController.navigate(R.id.homeFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.homeFragment, true)
                            .build());
                }else if (itemId == R.id.contact && currentDestId != R.id.contactFragment) {
                    navController.navigate(R.id.contactFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.contactFragment, true)
                            .build());
                } else if (itemId == R.id.setting && currentDestId != R.id.settingFragment) {
                    navController.navigate(R.id.settingFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.settingFragment, true)
                            .build());
                } else if (itemId == R.id.search && currentDestId != R.id.findPeopleFragment) {
                    navController.navigate(R.id.findPeopleFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.findPeopleFragment, true)
                            .build());
                }
                return true;
            });

            navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
                int destinationId =navDestination.getId();
                if (hiddenBottomBarFrag.contains(navDestination.getId())) {
                    binding.bottom.setVisibility(View.GONE);
                } else {
                    binding.bottom.setVisibility(View.VISIBLE);
                }

                if (destinationId == R.id.homeFragment) {
                    binding.bottom.setSelectedItemId(R.id.chat);
                } else if (destinationId == R.id.contactFragment) {
                    binding.bottom.setSelectedItemId(R.id.contact);
                } else if (destinationId == R.id.settingFragment) {
                    binding.bottom.setSelectedItemId(R.id.setting);
                }
            });
            String uid = UserRepo.getCurrentUserUID();
            if(uid!=null)
            {
//                navController.navigate(R.id.homeFragment, null, new NavOptions.Builder()
//                        .setPopUpTo(R.id.loginFragment, true)
//                        .build());
                Map<String, Object> map = new HashMap<>();
                map.put("status", "online");
                FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL).getReference("users").child(uid)
                        .setValue(map)
                        .addOnSuccessListener(aVoid -> Log.e("Firebaseccc", "User created successfully"))
                        .addOnFailureListener(e -> Log.e("Firebaseccc", "Failed to create user: " + e.getMessage()));

            }else {
                Log.e("Firebaseccc", "none");
            }
//            UserRepo.getUserUID().observe(this, userUID -> {
//                if (userUID != null) {
//
//                }
//            });
        });
        userUID.observe(this,string -> {
            if(userUID.getValue()!=null&&!userUID.getValue().isEmpty()){
                userRef= FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL).getReference("users").child(userUID.getValue()).child("status");
                userRef.onDisconnect().setValue("offline");
            }
        });



    }
    private Handler handler = new Handler();

    @Override
    protected void onPause() {
        super.onPause();
        if(userUID!=null&&!userUID.getValue().isEmpty())
        {
            seOffLine();
//            handler.postDelayed(this::seOffLine, 10000);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(userUID!=null&&!userUID.getValue().isEmpty()){
                userRef.setValue("online")
                .addOnSuccessListener(runnable -> {
                    Log.e( "onResume:: ","ok" );
                })
                .addOnFailureListener(e -> {
                    Log.e( "onResume:: ",e.getMessage() );
                });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(userUID!=null&&!userUID.getValue().isEmpty()){
            seOffLine();
        }
    }

    //    @Override
//    public void onTrimMemory(int level) {
//        super.onTrimMemory(level);
//        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
//            seOffLine();
//        }
//    }
    private void seOffLine(){
        if (UserRepo.getCurrentUserUID()!=null||!UserRepo.getCurrentUserUID().isEmpty()) {
            userRef.setValue("offline");
        }

    }
}
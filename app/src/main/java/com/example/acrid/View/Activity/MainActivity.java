package com.example.acrid.View.Activity;

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
            R.id.chatFragment,
            R.id.findPeopleFragment
    ));
    private String userUID;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "fcm_default_channel";
            String channelName = "FCM Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Channel for FCM notifications");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        userUID =UserRepo.getCurrentUserUID();
        ////init trang thái online
        if(userUID!=null&&!userUID.isEmpty()){
            userRef= FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL).getReference("users").child(userUID).child("status");
            userRef.onDisconnect().setValue("offline");
        }

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

        if (!Settings.canDrawOverlays(this)) {
            Log.d("ChatHead", "Chưa có quyền overlay, yêu cầu quyền...");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 101);
        } else {
            Log.d("ChatHead", "Đã có quyền overlay, bắt đầu service...");
            Intent serviceIntent = new Intent(this, ChatHeadService.class);
            startService(serviceIntent);
        }






    }
    private Handler handler = new Handler();

    @Override
    protected void onPause() {
        super.onPause();
        if(userUID!=null&&!userUID.isEmpty())
        {
            handler.postDelayed(this::seOffLine, 10000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(userUID!=null&&!userUID.isEmpty()){
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
        if(UserRepo.getCurrentUserUID().isEmpty()) return;
        seOffLine();
    }
    @Override
    protected void onStop() {
        super.onStop();
//        seOffLine();
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
            FirebaseDatabase.getInstance(BuildConfig.FIREBASE_SOCKET_URL)
                    .getReference("users")
                    .child(UserRepo.getCurrentUserUID())
                    .child("status")
                    .setValue("offline")
                    .addOnSuccessListener(aVoid -> Log.e("onStop:: ", "User offline"))
                    .addOnFailureListener(e -> Log.e("onStop:: ", e.getMessage()));
        }

    }
}
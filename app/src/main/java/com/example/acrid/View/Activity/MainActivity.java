package com.example.acrid.View.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.acrid.Helper.AuthRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.R;
import com.example.acrid.View.Fragment.LoginFragment;
import com.example.acrid.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final Set<Integer> hiddenBottomBarFrag= new HashSet<>(Arrays.asList(
            R.id.signUpFragment,
            R.id.loginFragment,
            R.id.findPeopleFragment
    ));
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

        binding.getRoot().post(()->{
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            binding.bottom.setOnItemReselectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.chat) {
                    navController.navigate(R.id.chatFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.chatFragment, true)
                            .build());
                } else if (itemId == R.id.contactFragment) {
                    navController.navigate(R.id.contactFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.contactFragment, true)
                            .build());
                } else if (itemId == R.id.setting) {
                    navController.navigate(R.id.settingFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.settingFragment, true)
                            .build());
                }
            });

            navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
                if (hiddenBottomBarFrag.contains(navDestination)) {
                    binding.bottom.setVisibility(View.GONE);
                } else {
                    binding.bottom.setVisibility(View.VISIBLE);
                }
            });


            UserRepo.getUserUID().observe(this, userUID -> {
                if (userUID != null) {
                    navController.navigate(R.id.findPeopleFragment, null, new NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .build());

                }
            });
        });






    }

}
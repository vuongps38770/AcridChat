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
            R.id.chatFragment,
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

            if(UserRepo.getCurrentUserUID()!=null)
            {
                navController.navigate(R.id.homeFragment, null, new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build());
            }
//            UserRepo.getUserUID().observe(this, userUID -> {
//                if (userUID != null) {
//
//                }
//            });
        });






    }

}
package com.example.acrid.View.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.acrid.R;
import com.example.acrid.databinding.FragmentSettingBinding;
import com.example.acrid.viewModel.SettingViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    SettingViewModel settingViewModel;
    NavController navController;
    FragmentSettingBinding binding;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        settingViewModel = new ViewModelProvider(requireActivity()).get(SettingViewModel.class);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_setting,container,false);
        binding.setViewModel(settingViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);




        obseveViewModel();





        binding.logout.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setNegativeButton("Huỷ", (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton("Đăng xuất", (dialogInterface, i) -> {
                        settingViewModel.logout();
                    })
                    .setMessage("Bạn có chắc muốn đăng xất khỏi tài khoản?")
                    .setTitle("Đăng xất khỏi tài khoản?");
            AlertDialog dialog = builder.create();
            dialog.show();

        });


        binding.editImg.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33)
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
                } else {
                    openBottomSheet();
                }
            } else { // Android 12 trở xuống
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    openBottomSheet();

                }
            }
        });



        requestPermissionLauncher = registerForActivityResult(

                new ActivityResultContracts.RequestPermission(),



                isGranted -> {
                    if (isGranted) {
                        openBottomSheet();
                    } else {
                        Toast.makeText(requireContext(), "Cần cấp quyền để chọn ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }



        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        settingViewModel.setImgBitmap(photo,requireContext());
                    }else {
                        Toast.makeText(requireContext(), "Đã huỷ", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        pickImageLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if(result.getResultCode()== Activity.RESULT_OK
                            &&result.getData()!=null
                    ){
                        Toast.makeText(requireContext(), "ok", Toast.LENGTH_SHORT).show();
                        Uri imgUri = result.getData().getData();
                        settingViewModel.setImgUri(imgUri,requireContext());
                    }else {
                        Toast.makeText(requireContext(), "Đã huỷ", Toast.LENGTH_SHORT).show();
                    }
                });
        binding.btnConfirm.setOnClickListener(view1 -> {
            settingViewModel.saveImg();
        });
        binding.btnDismiss.setOnClickListener(view1 -> {
            Toast.makeText(requireContext(), "click", Toast.LENGTH_SHORT).show();
            settingViewModel.dismiss();
        });
        binding.info.setOnClickListener(view1 -> {
            navController.navigate(R.id.action_settingFragment_to_infoFragment);
        });
        binding.status.setSelected(true);

        binding.imgAVT.setOnClickListener(view1 -> {
            settingViewModel.openImgDialog(requireActivity().getSupportFragmentManager());
        });
    }



    private void obseveViewModel() {

        settingViewModel.isLoggedOut.observe(getViewLifecycleOwner(),aBoolean -> {
            if(aBoolean){
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                navController.navigate(R.id.loginFragment,null,new NavOptions.Builder()
                        .setPopUpTo(navController.getGraph().getStartDestinationId(), true)
                        .build()
                );
            }

        });
        settingViewModel.bitmapImg.observe(getViewLifecycleOwner(),bitmap -> {
            if(bitmap==null) return;
            binding.imgAVT.setImageBitmap(bitmap);
        });



        settingViewModel.uriImg.observe(getViewLifecycleOwner(),uri -> {
            if(uri == null)return;
            binding.imgAVT.setImageURI(uri);
        });

        settingViewModel.avtUrl.observe(getViewLifecycleOwner(),url -> {
            if(url == null){
                return;
            }
            binding.imgAVT.setImageURI(null);
            binding.imgAVT.setImageBitmap(null);
            Glide.with(binding.imgAVT)
                    .load(url)
                    .placeholder(R.drawable.load)
                    .error(R.drawable.err)
                    .into(binding.imgAVT);
        });
        settingViewModel.errMess.observe(getViewLifecycleOwner(),string -> {
            if (string.isEmpty())return;
            Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show();
        });




    }

    private void openBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = requireActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_layout,null);
        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            openImagePicker();
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            openCamera();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();

    }

    private void openImagePicker(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getActivity().getPackageManager())==null) {
            Toast.makeText(requireContext(), "Thiết bị không hỗ trợ mở camera", Toast.LENGTH_SHORT).show();
            return;
        }
        takePhotoLauncher.launch(intent);
    }

}
package com.example.acrid.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.acrid.R;
import com.example.acrid.databinding.FragmentInfoBinding;
import com.example.acrid.viewModel.InfoViewModel;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {



    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    InfoViewModel infoViewModel;
    FragmentInfoBinding binding;
    NavController navController;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_info,container,false);
        infoViewModel = new ViewModelProvider(this).get(InfoViewModel.class);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setViewModel(infoViewModel);
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        obseveViewModel();
        binding.edtBirthDate.setOnClickListener(view1 -> {
            infoViewModel.openChoseDateDialog(requireContext());
        });
        binding.edtDescription.setOnClickListener(view1 -> {
            infoViewModel.editDes(requireContext());
        });
        binding.edtName.setOnClickListener(view1 -> {
            infoViewModel.editName(requireContext());
        });
        binding.btnBack.setOnClickListener(view1 -> {
            navController.popBackStack();
        });

    }

    private void obseveViewModel() {

        infoViewModel.description.observe(getViewLifecycleOwner(),string -> {
            if(string.trim().isEmpty()){
                binding.description.setText("Chưa có mô tả");
            }else {
                binding.description.setText(string);
            }
        });
    }
}
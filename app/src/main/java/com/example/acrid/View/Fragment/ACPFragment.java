package com.example.acrid.View.Fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.acrid.R;
import com.example.acrid.adapter.ACP_QueqeAdapter;
import com.example.acrid.adapter.FindPeopleAdapter;
import com.example.acrid.databinding.FragmentACPBinding;
import com.example.acrid.viewModel.ContactViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ACPFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ACPFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ACPFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ACPFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ACPFragment newInstance(String param1, String param2) {
        ACPFragment fragment = new ACPFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    FragmentACPBinding binding;
    ContactViewModel viewModel;
    NavController navController;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_a_c_p,container,false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        binding.setViewmodel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ACP_QueqeAdapter adapter = new ACP_QueqeAdapter(getContext(),new ArrayList<>());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        binding.recycler.setAdapter(adapter);
        viewModel.ACPList.observe(getViewLifecycleOwner(), list -> {
            Toast.makeText(getContext(), list.size()+"", Toast.LENGTH_SHORT).show();
            adapter.setData(list);
        });

        adapter.setOnAddFriendClickedListener((person, pos) -> {
            Log.e("onViewCreatedd: ",person.getEmail() );
            viewModel.acpFriend(person);
        });

        Toolbar toolbar = binding.getRoot().findViewById(R.id.toolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.post(() -> {
            toolbar.setTitle("Danh sách chờ kết bạn");
            Drawable navIcon = toolbar.getNavigationIcon();
            if (navIcon != null) {
                navIcon.setTint(ContextCompat.getColor(requireContext(), R.color.white));
            }
        });

        toolbar.setNavigationOnClickListener(view1 -> {
            navController.popBackStack();
        });

    }
}
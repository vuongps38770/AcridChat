package com.example.acrid.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.SearchView;
import android.widget.Toast;

import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.People;
import com.example.acrid.R;
import com.example.acrid.State.SimpleCallBack;
import com.example.acrid.adapter.FindPeopleAdapter;
import com.example.acrid.databinding.FragmentFindPeopleBinding;
import com.example.acrid.viewModel.FindPeopleViewModel;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindPeopleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindPeopleFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FindPeopleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindPeopleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FindPeopleFragment newInstance(String param1, String param2) {
        FindPeopleFragment fragment = new FindPeopleFragment();
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
    FindPeopleViewModel findPeopleViewModel;
    FragmentFindPeopleBinding binding;
    FindPeopleAdapter adapter;
    NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_people, container, false);
        findPeopleViewModel = new ViewModelProvider(this).get(FindPeopleViewModel.class);
        binding.setViewModel(findPeopleViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        navController= Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /// setup adapter
        adapter = new FindPeopleAdapter(getContext(), new ArrayList<>(), UserRepo.getCurrentUserUID());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        binding.searchbar.requestFocus();
        binding.btnBack.setOnClickListener(view1 -> {
            navController.popBackStack();
        });
        //add friend pos là cập nhật vị trí hiên tại
        adapter.setOnAddFriendClickedListener((people,pos) -> {
            Log.e("onViewCreated: ", pos+"");
            findPeopleViewModel.changePos.setValue(pos);
        });

        ///theo dõi data list người yêu cầu kết bạn
        findPeopleViewModel.peopleList.observe(getViewLifecycleOwner(), people -> {
            Log.d("FindPeopleFragment", "onViewCreated: " + people.size()+"");
            adapter.setData(people);
        });


        /// theo doi có lỗi thì toast
        findPeopleViewModel.tempERR.observe(getViewLifecycleOwner(),string -> {
            if(!string.isEmpty()) Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
        });
        findPeopleViewModel.changePos.observe(getViewLifecycleOwner(),integer -> {
            if (integer != null && integer != -1) {
                findPeopleViewModel.addFriendClick();
                findPeopleViewModel.changePos.setValue(-1);
            }
        });

        ///theo dõi data trong ô tìm kiếm
        binding.searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                findPeopleViewModel.search.setValue(s);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                findPeopleViewModel.search.setValue(s);
                return false;
            }
        });
    }
}
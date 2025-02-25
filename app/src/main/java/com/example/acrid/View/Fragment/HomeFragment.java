package com.example.acrid.View.Fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.acrid.Constant.Const;
import com.example.acrid.Model.Conversation;
import com.example.acrid.R;
import com.example.acrid.View.Dialog.PopupBuilder;
import com.example.acrid.adapter.ConversationAdapter;
import com.example.acrid.databinding.FragmentHomeBinding;
import com.example.acrid.databinding.FragmentLoginBinding;
import com.example.acrid.viewModel.HomeViewModel;
import com.example.acrid.viewModel.LoginViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding.setViewModel(homeViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConversationAdapter adapter = new ConversationAdapter(getContext(),new ArrayList<>());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        binding.recycler.setAdapter(adapter);
        adapter.setOnConversationClickListener(conversation -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Const.APP_FRIEND_BUNDLE_NAME,conversation.getPartner());
            bundle.putString(Const.APP_CHAT_UID_BUNDLE_NAME,conversation.getDocumentId());
            navController.navigate(R.id.action_homeFragment_to_chatFragment,bundle);
        });
        adapter.setOnConversationLongClickListener((conversation, anchorView) -> {
            new PopupBuilder(requireContext())
                    .addItem("Xoá đoạn chat", Color.RED,view1 -> {})
                    .addItem("Đánh dấu là đã đọc", view1 -> {})
                    .build();
        });
        homeViewModel.conversationListData.observe(getViewLifecycleOwner(), adapter::setData);

        binding.search.setOnClickListener(view1 -> {
            navController.navigate(R.id.action_homeFragment_to_findPeopleFragment);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // nhấn lần 2( nếu tg chờ thấp hơn 2s thì thoát)
                if (System.currentTimeMillis() - backPressedTime < 2000) {
                    requireActivity().finish();
                } else {
                    // nhấn lần 1 set tg chờ là mili hiện tại
                    Toast.makeText(requireContext(), "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }
    private long backPressedTime = 0;
}
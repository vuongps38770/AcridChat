package com.example.acrid.View.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.acrid.Constant.Const;
import com.example.acrid.Helper.ChatRepo;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.Message;
import com.example.acrid.R;
import com.example.acrid.adapter.MessageAdapter;
import com.example.acrid.databinding.FragmentChatBinding;
import com.example.acrid.viewModel.ChatViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
    FragmentChatBinding binding;
    ChatViewModel chatViewModel;
    NavController navController;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chat,container,false);
        binding.setViewModel(chatViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recycler.setOnTouchListener((view1, motionEvent) -> {
            hideKeyboard();
            return false;
        });

        Bundle bundle = getArguments();
        if(bundle==null) return;


        String chatUID=bundle.getString(Const.APP_CHAT_UID_BUNDLE_NAME);
        if(chatUID==null||chatUID.isEmpty()){
            return;
        }
        chatViewModel.conversationsID.setValue(chatUID);



        MessageAdapter adapter = new MessageAdapter(requireContext(),new ArrayList<>(),UserRepo.getCurrentUserUID());
        LinearLayoutManager manager =new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        manager.setStackFromEnd(true);
        binding.recycler.setLayoutManager(manager);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setVerticalScrollBarEnabled(true);
        binding.recycler.setScrollbarFadingEnabled(false);




        Friend friend =(Friend) bundle.getSerializable(Const.APP_FRIEND_BUNDLE_NAME);
        if(friend==null){
            Toast.makeText(getContext(), "ERR, MISSING FRUID DATA", Toast.LENGTH_SHORT).show();
            return;
        }
        chatViewModel.partnerUID.setValue(friend.getUserUID());
        chatViewModel.partnerStatus.observe(getViewLifecycleOwner(),aBoolean -> {
            if(aBoolean){
                binding.status.setText("Online");
                binding.status.setTextColor(ContextCompat.getColor(requireContext(),R.color.online));
            }else {
                binding.status.setText("Offline");
                binding.status.setTextColor(ContextCompat.getColor(requireContext(),R.color.gray));

            }
        });

        chatViewModel.chatData.observe(getViewLifecycleOwner(), messages -> {
            for (Message m :messages){
                Log.e("onViewCreated: ",m.getTimestamp()+"" );
            }
            adapter.setData(messages);
            if (!messages.isEmpty()) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                    int totalItems = messages.size();
                    if (lastVisibleItem >= totalItems - 3&&messages.size()>1) {
                        binding.recycler.smoothScrollToPosition(messages.size() );
                    }
                }
            }
        });

        binding.btnSendMessage.setOnClickListener(view1 -> {
            chatViewModel.sendMessage(friend.getToken());
        });

        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition <= 5) {
                    chatViewModel.loadOlderMessages();
                }
            }
        });

        binding.name.setText(friend.getIDName());


        Glide.with(binding.avt).load(friend.getProfileIMG())
                .placeholder(R.drawable.load)
                .error(R.drawable.img)
                .into(binding.avt);


        binding.imgBack.setOnClickListener(view1 -> {
            navController.popBackStack();
        });
    }
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
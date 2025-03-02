package com.example.acrid.View.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.acrid.Constant.Const;
import com.example.acrid.Model.Friend;
import com.example.acrid.R;
import com.example.acrid.View.Dialog.PopupBuilder;
import com.example.acrid.adapter.FriendAdapter;
import com.example.acrid.databinding.FragmentContactBinding;
import com.example.acrid.viewModel.ContactViewModel;
import com.google.android.play.integrity.internal.f;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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
    FragmentContactBinding binding;
    ContactViewModel contactViewModel;
    NavController navController;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_contact,container,false);
        contactViewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        binding.setViewmodel(contactViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.scrollView.setOnTouchListener((view, motionEvent) -> {
            hideKeyboard();
            return true;
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ///set up adapter rỗng
        FriendAdapter adapter = new FriendAdapter(getContext(),new ArrayList<>());
        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        binding.recycler.setAdapter(adapter);
        adapter.setOnSeeMoreClickListener((view1, friend, pos) -> {
            new PopupBuilder(getContext())
                    .addItem("nhắn tin",view2 -> {
                        contactViewModel.selectedFriend.postValue(friend);
                        contactViewModel.gotoChat(friend.getUserUID());
                    })
                    .addItem("Xem thông tin",view2 -> {
                        contactViewModel.showProfile(friend.getUserUID(),requireContext());
                    })
                    .addItem("Huỷ kết bạn", Color.RED,view2 -> {

                    })

                    .build()
                    .show(view1);
        });
        contactViewModel.isOnError.observe(getViewLifecycleOwner(),aBoolean -> {
            if(aBoolean) Toast.makeText(getContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
        });
        contactViewModel.conversationID.observe(getViewLifecycleOwner(),converUID -> {
            if(converUID.isEmpty()) return;
            Toast.makeText(getContext(), converUID, Toast.LENGTH_SHORT).show();
            Bundle bundle = new Bundle();
            bundle.putString(Const.APP_CHAT_UID_BUNDLE_NAME, converUID);
            if(contactViewModel.selectedFriend.getValue()!=null){
                bundle.putSerializable(Const.APP_FRIEND_BUNDLE_NAME,contactViewModel.selectedFriend.getValue());
            }
            navController.navigate(R.id.action_contactFragment_to_chatFragment,bundle);
        });

        ///theo dõi data bạn bè
        contactViewModel.friendListData.observe(getViewLifecycleOwner(), adapter::setData);

        ///ấn nút xem danh sách chiwf kết bạn
        binding.gotoACP.setOnClickListener(view1 -> {
            navController.navigate(R.id.action_contactFragment_to_ACPFragment);
        });
        /// setup toolbar
        Toolbar toolbar = view.findViewById(R.id.toolBar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("Tìm kiếm bạn bè");


        /// setup search
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.contact_search_menu, menu);
                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint("Nhập tên bạn bè");
                EditText editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                editText.setHintTextColor(ContextCompat.getColor(requireContext(),R.color.mainChild));
                editText.setTextColor(Color.WHITE);
                ImageView backIcon = searchView.findViewById(androidx.appcompat.R.id.search_go_btn);
                if (backIcon != null) {
                    backIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.mainChild), PorterDuff.Mode.SRC_IN);
                }
                ImageView backIcon2 = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
                if (backIcon2 != null) {
                    backIcon2.setColorFilter(ContextCompat.getColor(requireContext(),R.color.mainChild), PorterDuff.Mode.SRC_ATOP);
                }
               if (toolbar.getNavigationIcon() != null) {
                   toolbar.getNavigationIcon().setColorFilter(ContextCompat.getColor(requireContext(),R.color.mainChild), PorterDuff.Mode.SRC_IN);

               }
                ///tìm kiếm
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                       ////////////////////
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                   //////////////////////////////
                        contactViewModel.search(s);
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onPause() {
        super.onPause();
        contactViewModel.selectedFriend.setValue(null);
        contactViewModel.conversationID.setValue("");
    }
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
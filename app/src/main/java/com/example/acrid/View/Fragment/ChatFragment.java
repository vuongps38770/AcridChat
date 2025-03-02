package com.example.acrid.View.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.acrid.Constant.Const;
import com.example.acrid.Constant.DB;
import com.example.acrid.Helper.UserRepo;
import com.example.acrid.Model.Friend;
import com.example.acrid.Model.GlobalData;
import com.example.acrid.R;
import com.example.acrid.View.Dialog.ImageDialogFragment;
import com.example.acrid.adapter.MessageAdapter;
import com.example.acrid.databinding.FragmentChatBinding;
import com.example.acrid.viewModel.ChatViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.play.core.integrity.p;

import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ChatFragment.class);

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
    private ActivityResultLauncher<Intent> pockImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
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
        ////token, uid, name, imgage
        Bundle bundle = getArguments();
        if(bundle==null) return;


        String chatUID=bundle.getString(Const.APP_CHAT_UID_BUNDLE_NAME);



        if(chatUID==null||chatUID.isEmpty()){
            return;
        }
        chatViewModel.conversationsID.setValue(chatUID);








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


        MessageAdapter adapter = new MessageAdapter(requireContext(),new ArrayList<>(),UserRepo.getCurrentUserUID(),friend.getUserUID(), binding.recycler);

        LinearLayoutManager manager =new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        manager.setStackFromEnd(true);
        binding.recycler.setLayoutManager(manager);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setVerticalScrollBarEnabled(true);
        binding.recycler.setScrollbarFadingEnabled(false);
        adapter.setItemClickListener(message -> {
            chatViewModel.clickItem(requireActivity().getSupportFragmentManager(),message);
        });





        chatViewModel.lastSeenMap.observe(getViewLifecycleOwner(), adapter::setLastSeenMap);
        chatViewModel.isPartnerTyping.observe(getViewLifecycleOwner(),isTyping -> {
            binding.typingStatus.setVisibility(isTyping?View.VISIBLE:View.GONE);
        });

        chatViewModel.chatData.observe(getViewLifecycleOwner(), messages -> {
            Log.e("onViewCreated: ", messages.size()+"");
            int oldSize = adapter.getItemCount();
            adapter.setData(messages);

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recycler.getLayoutManager();
            if (layoutManager != null) {
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                // Trường hợp load tin nhắn cũ -> Giữ nguyên vị trí
                if (messages.size() > oldSize) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    View firstVisibleView = layoutManager.findViewByPosition(firstVisiblePosition);
                    int offset = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

                    binding.recycler.post(() -> {
                        layoutManager.scrollToPositionWithOffset(firstVisiblePosition + (messages.size() - oldSize), offset);
                    });

                }
                // Trường hợp có tin nhắn mới -> Cuộn xuống nếu đang ở gần cuối
                if (lastVisibleItem >= oldSize - 3 && !messages.isEmpty()) {
                    binding.recycler.postDelayed(() ->
                            binding.recycler.smoothScrollToPosition(messages.size() - 1), 100);
                }
            }
        });


        binding.btnSendMessage.setOnClickListener(view1 -> {
            chatViewModel.sendMessage(friend.getToken());
            Log.e("onViewCreated: ",friend.getIDName()+"///"+friend.getToken() );

        });




        chatViewModel.parnerName.postValue(friend.getIDName());









        chatViewModel.message.observe(getViewLifecycleOwner(),string -> {
            if(string.trim().isEmpty()){
                binding.btnSendMessage.setVisibility(View.GONE);
                binding.selectImg.setVisibility(View.VISIBLE);
            }else {
                binding.btnSendMessage.setVisibility(View.VISIBLE);
                binding.selectImg.setVisibility(View.GONE);
            }
        });


        chatViewModel.type.observe(getViewLifecycleOwner(),string -> {
            if(string.equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.TEXT.toString())){
                binding.holder.setVisibility(View.GONE);
                binding.edtMessage.setVisibility(View.VISIBLE);
                if(chatViewModel.message.getValue().trim().isEmpty()){
                    binding.selectImg.setVisibility(View.VISIBLE);
                    binding.btnSendMessage.setVisibility(View.GONE);
                }


            } else if (string.equals(DB.MESSAGES_COLLECTION.MESSAGE_TYPE.IMAGE.toString())) {
                binding.holder.setVisibility(View.VISIBLE);
                binding.edtMessage.setVisibility(View.GONE);

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
                        binding.imgHolder.setImageBitmap(photo);
                        binding.imgHolder.setVisibility(View.VISIBLE);
                        binding.imgHolder.setOnClickListener(view1 -> {
                            binding.imgHolder.setImageBitmap(null);
                            binding.imgHolder.setVisibility(View.GONE);
                            chatViewModel.image.postValue(null);
                        });
                        chatViewModel.image.postValue(bitmapToFile(requireContext(),photo));
                    }else {

                    }
                }
        );

        pockImageLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if(result.getResultCode()== Activity.RESULT_OK
                            &&result.getData()!=null
                    ){
                        Toast.makeText(requireContext(), "ok", Toast.LENGTH_SHORT).show();
                        Uri imgUri = result.getData().getData();
                        binding.imgHolder.setVisibility(View.VISIBLE);
                        binding.imgHolder.setImageURI(imgUri);
                        binding.imgHolder.setOnClickListener(view1 -> {
                            binding.imgHolder.setImageURI(null);
                            binding.imgHolder.setVisibility(View.GONE);
                            chatViewModel.image.postValue(null);
                        });
                        try {
                            chatViewModel.image.postValue(uriToFile(requireContext(),imgUri));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        Toast.makeText(requireContext(), "Đã huỷ", Toast.LENGTH_SHORT).show();
                    }
                });


        binding.selectImg.setOnClickListener(view1 -> {

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

        chatViewModel.image.observe(getViewLifecycleOwner(),file -> {
            if(file==null){
                binding.imgHolder.setVisibility(View.GONE);
                binding.holder.setText("Chọn ảnh");

            }else {
                binding.imgHolder.setVisibility(View.VISIBLE);
                binding.holder.setText("Gửi ảnh");
                binding.selectImg.setVisibility(View.GONE);
                binding.btnSendMessage.setVisibility(View.VISIBLE);
            }
        });
        chatViewModel.notifiPos.observe(getViewLifecycleOwner(),integer -> {
            if(integer<0) return;
            adapter.notifyItemChanged(integer);
        });


        binding.edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("onTextChanged: ", charSequence.toString());
                chatViewModel.setIsUserTyping(true);
                typingHandler.removeCallbacks(typingRunnable);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                typingRunnable = () -> chatViewModel.setIsUserTyping(false);
                typingHandler.postDelayed(typingRunnable, 1000);
            }
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(adapter.getItemCount()<20) return;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition <= 3&&hasMoreMessages) {
                    Toast.makeText(requireContext(), "loadding", Toast.LENGTH_SHORT).show();
                    chatViewModel.loadOlderMessages(aBoolean -> {
                        hasMoreMessages=aBoolean;
                    });
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



        binding.avt.setOnClickListener(v -> {

            ImageDialogFragment dialogFragment = new ImageDialogFragment(friend.getProfileIMG());
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "image_dialog");
        });




    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalData.getInstance().setCurentConversationID("");

    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private Handler typingHandler = new Handler();
    private Runnable typingRunnable;
    private boolean hasMoreMessages = true;
    @Override
    public void onPause() {
        super.onPause();
        chatViewModel.isChatScreenActive.postValue(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        chatViewModel.isChatScreenActive.postValue(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        pockImageLauncher.launch(intent);
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getActivity().getPackageManager())==null) {
            Toast.makeText(requireContext(), "Thiết bị không hỗ trợ mở camera", Toast.LENGTH_SHORT).show();
            return;
        }
        takePhotoLauncher.launch(intent);
    }
    private File bitmapToFile(Context context, Bitmap bitmap) {
        File file = new File(context.getCacheDir(), "temp_image.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    private File uriToFile(Context context, Uri uri) throws IOException {
        File file = new File(context.getCacheDir(), "temp_image.jpg");
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return file;
    }



}
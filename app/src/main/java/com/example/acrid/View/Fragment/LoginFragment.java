package com.example.acrid.View.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.acrid.R;
import com.example.acrid.State.LoginError;
import com.example.acrid.State.LoginState;
import com.example.acrid.databinding.FragmentLoginBinding;
import com.example.acrid.viewModel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;
    private NavController navController;
    private SharedPreferences sharedPreferences;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        // dùng data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        //khai báo viewmodel
        loginViewModel= new ViewModelProvider(this).get(LoginViewModel.class);

        //gán viewmodel cho binding
        binding.setViewModel(loginViewModel);

        //gán lifecycle cho binding
        binding.setLifecycleOwner(getViewLifecycleOwner());

        //khai báo navController
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        ///ktra sharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("Login", Context.MODE_PRIVATE);
        if(sharedPreferences!=null){
            boolean isauth = sharedPreferences.getBoolean("isAuthed",false);
            if(isauth){
                navController.navigate(R.id.homeFragment, null, new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build());
            }
        }


        //cho getroot trả về view (cái này quan trọng)
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ///đi tới trang đăng ký
        binding.txtSignUp.setOnClickListener(v -> {
            navController.navigate(R.id.action_loginFragment_to_signUpFragment,null,
                    new NavOptions.Builder()
                            .setPopUpTo(R.id.signUpFragment, true)
                            .build());
        });
        //ấn nút đăng nhập bằng email và password
        binding.btnLoginWEmailnPW.setOnClickListener(v -> {
            loginViewModel.loginWithEmailAndPW();
        });

        loginViewModel.loginState.observe(getViewLifecycleOwner(), loginState -> {
            if(loginState instanceof LoginState.Loading){
                Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show();
            }
            if(loginState instanceof LoginState.Success){

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isAuthed",true);
                editor.apply();
                navController.navigate(R.id.action_loginFragment_to_homeFragment);
            }
            if(loginState instanceof LoginState.Error){
                LoginState.Error error = (LoginState.Error) loginState;
                if(error.getErrors().contains(LoginError.EMPTY_EMAIL)){
                    binding.boxEmailWrapper.setError("Không được để trống email");
                }
                if (error.getErrors().contains(LoginError.EMPTY_PASSWORD)){
                    binding.boxEmailWrapper.setError("Vui lòng nhập mật khẩu");
                }
                if (error.getErrors().contains(LoginError.UNREGISTERED_EMAIL)){
                    Toast.makeText(requireContext(), "Email này chưa được đăng ký", Toast.LENGTH_SHORT).show();
                }
                if (error.getErrors().contains(LoginError.INVALID_CREDENTIALS)){
                    Toast.makeText(requireContext(), "Tài khoản hặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                }
                if (error.getErrors().contains(LoginError.OTHER_ERROR)) {
                    Toast.makeText(requireContext(), "Lỗi!, không thể đăng nhập", Toast.LENGTH_SHORT).show();
                };
            }
        });
//        AnimationDrawable animation = (AnimationDrawable) binding.title.getTextColors();
//        animation.start();
//        mAuth = FirebaseAuth.getInstance();
//
//        // Cấu hình đăng nhập Google
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))  // Thay thế bằng Web Client ID từ Firebase
//                .requestEmail()
//                .build();

//        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
//
//        googleSignInLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                        try {
//                            GoogleSignInAccount account = task.getResult(ApiException.class);
//                            firebaseAuthWithGoogle(account.getIdToken());
//                        } catch (ApiException e) {
//                            Log.e("GoogleSignIn", "Google sign-in failed", e);
//                        }
//                    }
//                });
//        binding.BtnLoginWGG.setOnClickListener(view1 -> {
//            signInWithGoogle();
//        });

    }
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Log.d("FirebaseAuth", "Đăng nhập thành công: " + user.getEmail());
//                        Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        Log.e("FirebaseAuth", "Đăng nhập thất bại", task.getException());
//                    }
//                });
//    }




}
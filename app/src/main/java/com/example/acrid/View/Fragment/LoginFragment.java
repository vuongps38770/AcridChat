package com.example.acrid.View.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.acrid.R;
import com.example.acrid.State.LoginError;
import com.example.acrid.State.LoginErrorState;
import com.example.acrid.State.LoginState;
import com.example.acrid.databinding.FragmentLoginBinding;
import com.example.acrid.viewModel.LoginViewModel;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

    }

}
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
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.acrid.R;
import com.example.acrid.State.SignUpErorr;
import com.example.acrid.State.SignUpState;
import com.example.acrid.databinding.FragmentSignUpBinding;
import com.example.acrid.viewModel.SignUpViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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
    private FragmentSignUpBinding binding;
    private NavController navController;
    private SignUpViewModel signUpViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false);
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        binding.setViewModel(signUpViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment,null,
                        new NavOptions.Builder().setPopUpTo(R.id.loginFragment,true).build());
            }
        });
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpViewModel.signUp();
            }
        });
        signUpViewModel.signUpState.observe(getViewLifecycleOwner(), signUpState -> {
            if(signUpState instanceof SignUpState.Success){
                navController.navigate(R.id.action_signUpFragment_to_loginFragment);
            }
            if(signUpState instanceof SignUpState.Loading){
                Toast.makeText(getContext(), "Loading", Toast.LENGTH_SHORT).show();
            }
            if(signUpState instanceof SignUpState.Error){
                SignUpState.Error error = (SignUpState.Error) signUpState;
                if(error.getError().contains(SignUpErorr.EMAIL_EMPTY)){
                    binding.boxEmailWrapper.setError("Email is empty");
                }else {
                    binding.boxEmailWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.PASSWORD_EMPTY)){
                    binding.boxPasswordWrapper.setError("Password is empty");
                }else {
                    binding.boxPasswordWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.RE_PASSWORD_EMPTY)){
                    binding.boxRePasswordWrapper.setError("Re-Password is empty");
                }else {
                    binding.boxRePasswordWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.NICKNAME_EMPTY)){
                    binding.boxNickNameWrapper.setError("NickName is empty");
                }else {
                    binding.boxNickNameWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.PASSWORD_NOT_MATCH)){
                    binding.boxRePasswordWrapper.setError("Password not match");
                }else {
                    binding.boxRePasswordWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.EMAIL_ALREADY_EXISTS)){
                    Toast.makeText(getContext(), "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                }
                if(error.getError().contains(SignUpErorr.EMAIL_INVALID)){
                    binding.boxEmailWrapper.setError("Email không hợp lệ");
                    binding.edtEmail.requestFocus();
                }  else {
                    binding.boxEmailWrapper.setError(null);
                }
                if(error.getError().contains(SignUpErorr.OTHER)){
                    Toast.makeText(getContext(), "Lỗi, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
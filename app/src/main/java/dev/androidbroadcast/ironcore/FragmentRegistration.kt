package dev.androidbroadcast.ironcore

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import dev.androidbroadcast.ironcore.databinding.FragmentRegistrationBinding
import java.io.IOException
import java.io.InputStreamReader

@AndroidEntryPoint
class FragmentRegistration : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.registerState.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_registration_to_profile)
            }
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val difficultyLevel = getSelectedDifficultyLevel()
            val userName = binding.etUsername.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty() && difficultyLevel != null) {
                authViewModel.register(email, password, userName, difficultyLevel)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedDifficultyLevel(): String? {
        return when (binding.radioGroupDifficulty.checkedRadioButtonId) {
            R.id.rb_beginner -> "beginner"
            R.id.rb_intermediate -> "intermediate"
            R.id.rb_advanced -> "advanced"
            else -> null
        }
    }
}





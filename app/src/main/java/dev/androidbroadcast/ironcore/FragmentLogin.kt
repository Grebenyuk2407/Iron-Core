package dev.androidbroadcast.ironcore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dev.androidbroadcast.ironcore.databinding.FragmentLoginBinding

class FragmentLogin : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Добавляем savedInstanceState

        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        // Проверка статуса "Запомнить меня" при запуске
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            findNavController().navigate(R.id.action_login_to_profile)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Если логин успешный и галочка "Запомнить меня" выбрана
                            if (binding.chkRememberMe.isChecked) {
                                saveLoginState(true) // Сохраняем статус логина
                            } else {
                                saveLoginState(false) // Сбрасываем статус логина
                            }
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_login_to_profile)
                        } else {
                            Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_registration)
        }
    }


    private fun saveLoginState(isRemembered: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("rememberMe", isRemembered)
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

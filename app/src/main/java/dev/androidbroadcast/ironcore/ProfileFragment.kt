package dev.androidbroadcast.ironcore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.androidbroadcast.ironcore.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfile(userId)
        }

        // Загрузка аватарки
        binding.btnEditProfile.setOnClickListener {
            pickImageFromGallery()
        }
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.getString("username")
                    val difficultyLevel = document.getString("difficultyLevel")
                    val tokenBalance = document.getLong("tokenBalance") ?: 0

                    // Отображаем данные в интерфейсе
                    binding.tvUserName.text = userName
                    binding.tvProgramLevel.text = "$difficultyLevel"
                    binding.tvTokenBalance.text = "Token Balance: $tokenBalance"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Ошибка загрузки профиля: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logoutUser() {
        // 1. Выход из Firebase
        auth.signOut()

        // 2. Очищаем данные о сохранённом логине
        val editor = sharedPreferences.edit()
        editor.putBoolean("rememberMe", false)
        editor.apply()

        // 3. Навигация на экран логина
        findNavController().navigate(R.id.action_profile_to_login)

        // Вывод сообщения для пользователя
        Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            binding.imgUserAvatar.setImageURI(data?.data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}

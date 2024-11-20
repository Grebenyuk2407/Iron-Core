package dev.androidbroadcast.ironcore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Привязка BottomNavigationView к NavController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.setupWithNavController(navController)

        // Скрытие/показ BottomNavigationView в зависимости от текущего фрагмента
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragmentLogin ||
                destination.id == R.id.fragmentRegistration ||
                destination.id == R.id.exerciseCameraFragment) {
                bottomNav.visibility = View.GONE // Скрываем меню
            } else {
                bottomNav.visibility = View.VISIBLE // Показываем меню
            }
        }
    }

    // Функции для явного управления видимостью BottomNavigationView
    fun hideBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.GONE
    }

    fun showBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE
    }
}



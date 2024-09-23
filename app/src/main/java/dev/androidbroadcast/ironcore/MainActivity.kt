package dev.androidbroadcast.ironcore

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Привязка BottomNavigationView к NavController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        NavigationUI.setupWithNavController(bottomNav, navController)

        // Если нужно, обработай навигацию по фрагментам и настроить возвращение в предыдущие экраны.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragmentLogin || destination.id == R.id.fragmentRegistration) {
                bottomNav.visibility = View.GONE // Скрываем нижнее меню на экранах входа и регистрации
            } else {
                bottomNav.visibility = View.VISIBLE // Показываем меню на других экранах
            }
        }
    }
}
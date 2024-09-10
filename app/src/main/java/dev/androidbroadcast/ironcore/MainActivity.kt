package dev.androidbroadcast.ironcore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Если тебе нужно добавить нижнее меню или тулбар, здесь ты можешь связать его с навигацией:
        // Например, если есть BottomNavigationView
        // val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        // NavigationUI.setupWithNavController(bottomNav, navController)
    }
}
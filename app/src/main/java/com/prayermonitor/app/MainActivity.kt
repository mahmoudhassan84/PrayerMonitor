package com.prayermonitor.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.prayermonitor.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_prayers, R.id.navigation_dashboard, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        
        // Check if user is logged in
        checkUserLoginStatus()
    }
    
    private fun checkUserLoginStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // User is not logged in, show a toast message
            Toast.makeText(this, "Please log in to track your prayers", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Check if user is signed in when activity starts
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // Navigate to profile fragment for login
            binding.navView.selectedItemId = R.id.navigation_profile
        }
    }
}

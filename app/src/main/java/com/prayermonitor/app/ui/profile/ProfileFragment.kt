package com.prayermonitor.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.prayermonitor.app.R
import com.prayermonitor.app.data.database.PrayerDatabase
import com.prayermonitor.app.data.model.User
import com.prayermonitor.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var authRepository: AuthRepository
    
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        
        // Initialize views
        emailEditText = root.findViewById(R.id.edit_email)
        passwordEditText = root.findViewById(R.id.edit_password)
        loginButton = root.findViewById(R.id.button_login)
        registerButton = root.findViewById(R.id.button_register)
        
        // Initialize repositories
        // Note: In a real app, you would use dependency injection
        val userDao = PrayerDatabase.getInstance(requireContext()).userDao()
        authRepository = AuthRepository(userDao)
        
        // Set up click listeners
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                register(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Check if user is already logged in
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            // User is already logged in, update UI
            updateUIForLoggedInUser(currentUser.email ?: "")
        }
        
        return root
    }
    
    private fun login(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val user = authRepository.login(email, password)
                updateUIForLoggedInUser(user.email ?: "")
                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun register(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val user = authRepository.register(email, password, email.substringBefore('@'))
                
                // Create user in local database
                val newUser = User(
                    id = user.uid,
                    email = email,
                    name = email.substringBefore('@')
                )
                authRepository.createUserInDatabase(newUser)
                
                updateUIForLoggedInUser(user.email ?: "")
                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateUIForLoggedInUser(email: String) {
        emailEditText.setText(email)
        emailEditText.isEnabled = false
        passwordEditText.setText("")
        passwordEditText.isEnabled = false
        loginButton.text = "Logout"
        loginButton.setOnClickListener {
            authRepository.logout()
            resetUI()
        }
        registerButton.visibility = View.GONE
    }
    
    private fun resetUI() {
        emailEditText.setText("")
        emailEditText.isEnabled = true
        passwordEditText.setText("")
        passwordEditText.isEnabled = true
        loginButton.text = getString(R.string.login)
        loginButton.setOnClickListener {
            login(emailEditText.text.toString(), passwordEditText.text.toString())
        }
        registerButton.visibility = View.VISIBLE
    }
}

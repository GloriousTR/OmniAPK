package com.omniapk.ui.settings

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseUser
import com.omniapk.BuildConfig
import com.omniapk.auth.GoogleAuthManager
import com.omniapk.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var authManager: GoogleAuthManager
    
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            authManager.handleSignInResult(
                task,
                onSuccess = { user -> updateUI(user) },
                onError = { e -> 
                    Toast.makeText(requireContext(), "Giriş hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvVersion.text = "Versiyon: ${BuildConfig.VERSION_NAME}"
        
        setupClickListeners()
        updateUI(authManager.getCurrentUser())
    }
    
    private fun setupClickListeners() {
        binding.btnGoogleSignIn.setOnClickListener {
            signInLauncher.launch(authManager.getSignInIntent())
        }
        
        binding.btnSignOut.setOnClickListener {
            authManager.signOut {
                updateUI(null)
                Toast.makeText(requireContext(), "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.layoutSignedOut.visibility = View.GONE
            binding.layoutSignedIn.visibility = View.VISIBLE
            binding.tvUserName.text = user.displayName ?: "Kullanıcı"
            binding.tvUserEmail.text = user.email ?: ""
        } else {
            binding.layoutSignedOut.visibility = View.VISIBLE
            binding.layoutSignedIn.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

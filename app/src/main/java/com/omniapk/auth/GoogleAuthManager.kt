package com.omniapk.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    private fun getWebClientId(): String {
        // This will be replaced with actual web client ID from google-services.json
        // For now, we'll use a placeholder that will be configured
        return context.getString(com.omniapk.R.string.default_web_client_id)
    }
    
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun isSignedIn(): Boolean = auth.currentUser != null
    
    fun handleSignInResult(task: Task<GoogleSignInAccount>, onSuccess: (FirebaseUser) -> Unit, onError: (Exception) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            android.util.Log.d("GoogleAuth", "Firebase Auth with Google: ${account.id}")
            firebaseAuthWithGoogle(account, onSuccess, onError)
        } catch (e: ApiException) {
            android.util.Log.e("GoogleAuth", "Google sign in failed", e)
            onError(e)
        }
    }
    
    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("GoogleAuth", "SignIn Success")
                    auth.currentUser?.let { onSuccess(it) }
                } else {
                    android.util.Log.e("GoogleAuth", "SignIn Failed", task.exception)
                    task.exception?.let { onError(it) }
                }
            }
    }
    
    fun signOut(onComplete: () -> Unit) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }
}

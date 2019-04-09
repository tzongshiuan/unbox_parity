package com.hsuanparty.unbox_parity.view.ui.setting

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel;
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.AuthStatus
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingViewModel @Inject constructor() : ViewModel(), Injectable {

    companion object {
        private val TAG = SettingViewModel::class.java.simpleName
    }

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var mPreferences: PreferencesHelper

    @Inject
    lateinit var mGso: GoogleSignInOptions

    private fun googleLogout(activity: Activity?) {
        // Google 登出
        GoogleSignIn.getClient(activity!!, mGso).signOut().addOnCompleteListener {
            //Toast.makeText(activity, "SingOut", Toast.LENGTH_LONG).show()
        }

        firebaseSingOut()
    }

    private fun facebookLogout() {
        // To logout Facebook programmatically
        LoginManager.getInstance().logOut()

        firebaseSingOut()
    }

    private fun firebaseSingOut() {
        // Firebase 登出
        mAuth.signOut()
    }

    fun logout(activity: Activity?) {
        when (mPreferences.authStatus) {
            AuthStatus.AUTH_GOOGLE -> googleLogout(activity)
            AuthStatus.AUTH_FACEBOOK -> facebookLogout()
            AuthStatus.AUTH_ANONYMOUS -> firebaseSingOut()
            else -> {}
        }

        mPreferences.isLogout = true
        activity?.finish()
    }
}

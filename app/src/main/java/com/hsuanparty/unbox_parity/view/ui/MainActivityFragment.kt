package com.hsuanparty.unbox_parity.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.hsuanparty.unbox_parity.databinding.FragmentMainBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.utils.LogMessage
import javax.inject.Inject


/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 * Description: A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment(), Injectable {

    companion object {
        private val TAG = MainActivityFragment::class.java.simpleName
    }

    @Inject
    lateinit var mDbManager: FirebaseDbManager

    private lateinit var mFragmentView: View

    private lateinit var mBinding: FragmentMainBinding

    private lateinit var callbackManager: CallbackManager
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogMessage.D(TAG, "onCreateView()")

        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()
    }

    private fun initUI() {
        LogMessage.D(TAG, "initUI()")

        initFbLogin()

//        mDbManager.setSingleValueEvent()
        mDbManager.setChildEvent()
//        mDbManager.add()
        mDbManager.clearAll()
    }

    private fun initFbLogin() {
        mBinding.fbLoginButton.setReadPermissions("email")
        mBinding.fbLoginButton.setReadPermissions("user_photos")
        mBinding.fbLoginButton.setReadPermissions("user_location")
        mBinding.fbLoginButton.fragment = this

        callbackManager = CallbackManager.Factory.create()
        mAuth = FirebaseAuth.getInstance()

        // Callback registration
        mBinding.fbLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken
                val credential = FacebookAuthProvider.getCredential(token.token)

                activity?.let {
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(
                            it
                        ) { task ->
                            if (task.isSuccessful) {
                                val user = mAuth.currentUser
                                LogMessage.D(TAG, "user name = $user")
                            } else {
                                LogMessage.D(TAG, "Login Failed")
                            }
                        }
                }
            }

            override fun onCancel() {
                // App code 取消登入時的處理
            }

            override fun onError(exception: FacebookException) {
                // App code 登入錯誤時的處理
            }
        })

        val fbProfile = Profile.getCurrentProfile()
        val accessTokenTracker = object: AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?, currentAccessToken: AccessToken?) {
                if (currentAccessToken == null) {
                    mAuth.signOut()
                    // TODO
                    // message.setText("請登入");
                }
            }
        }

        // FirebaseAuth.getInstance().signOut()
    }
}

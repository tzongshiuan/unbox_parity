package com.hsuanparty.unbox_parity.view.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.facebook.*
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.FragmentMainBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.AuthStatus
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.SimpleDelayTask
import com.tsunghsuanparty.textanimlib.slide.SlideAnimation
import org.json.JSONException
import java.io.IOException
import javax.inject.Inject


/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 * Description: A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment(), Injectable {

    companion object {
        private val TAG = MainActivityFragment::class.java.simpleName

        private const val GOOGLE_SIGN_IN = 128
    }

    @Inject
    lateinit var mDbManager: FirebaseDbManager

    @Inject
    lateinit var mCallbackManager: CallbackManager

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var mPreferences: PreferencesHelper

    @Inject
    lateinit var mGso: GoogleSignInOptions

    private lateinit var mFragmentView: View

    private lateinit var mBinding: FragmentMainBinding

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LogMessage.D(TAG, "onCreateView()")

        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        initUI()

        initSetting()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogMessage.D(TAG, "onActivityResult()")

        if (requestCode == GOOGLE_SIGN_IN){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess){
                val account = result.signInAccount
                //取得使用者並試登入
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            }
            return
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        LogMessage.D(TAG, "initUI()")

        initGoogleAuth()
        initFacebookAuth()
        initAnonymousAuth()

        mBinding.loadText.visibility = View.GONE
        mBinding.googleLoginButton.isEnabled = true
        mBinding.fbLoginButton.isEnabled = true
        mBinding.anonymousLoginBtn.isEnabled = true
        val slideAnimation = SlideAnimation()
        slideAnimation.initSettings(ContextCompat.getColor(context!!, R.color.white)
            , ContextCompat.getColor(context!!, R.color.orange)
            , SlideAnimation.ANIM_FAST)
        slideAnimation.startAnimation(mBinding.loadText)

//        mDbManager.setSingleValueEvent()
        mDbManager.setChildEvent()
//        mDbManager.add()
        mDbManager.clearAll()
    }

    private fun initGoogleAuth() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(activity!!) {
                    Toast.makeText(context, "Google", Toast.LENGTH_LONG).show()
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
                .build()
        }

        mBinding.googleLoginButton.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }

        mBinding.googleLogoutBtn.setOnClickListener {
            // Google 登出
            GoogleSignIn.getClient(activity!!, mGso).signOut().addOnCompleteListener {
                Toast.makeText(context, "SingOut", Toast.LENGTH_LONG).show()
            }
            firebaseSingOut()
        }
    }

    //登入 Firebase
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity!!,
                    OnCompleteListener<AuthResult> { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(context, "Google sign in failed", Toast.LENGTH_LONG).show()
                        } else {
                            LogMessage.D(TAG, "Sing in name:"+account.displayName)
                            startSearchFragment(AuthStatus.AUTH_GOOGLE)
                        }
                    })
    }

    private fun firebaseSingOut() {
        // Firebase 登出
        mAuth.signOut()
    }

    private fun initFacebookAuth() {
        mBinding.fbLoginButton.setReadPermissions("public_profile")
        mBinding.fbLoginButton.setReadPermissions("email")
        //mBinding.fbLoginButton.setReadPermissions("user_friends")
        //mBinding.fbLoginButton.setReadPermissions("user_photos")
        //mBinding.fbLoginButton.setReadPermissions("user_location")
        mBinding.fbLoginButton.fragment = this

        // Callback registration
        mBinding.fbLoginButton.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken
                val credential = FacebookAuthProvider.getCredential(token.token)

                LogMessage.D(TAG, "Facebook getApplicationId: " + token.applicationId);
                LogMessage.D(TAG, "Facebook getUserId: " + token.userId)
                LogMessage.D(TAG, "Facebook getExpires: " + token.expires)
                LogMessage.D(TAG, "Facebook getLastRefresh: " + token.lastRefresh)
                LogMessage.D(TAG, "Facebook getToken: " + token.token)
                LogMessage.D(TAG, "Facebook getSource: " + token.source)
                LogMessage.D(TAG, "Facebook getRecentlyGrantedPermissions: " + loginResult.recentlyGrantedPermissions)
                LogMessage.D(TAG, "Facebook getRecentlyDeniedPermissions: " + loginResult.recentlyDeniedPermissions)

                sendFbInfoRequest(token)

                activity?.let {
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(
                            it
                        ) { task ->
                            if (task.isSuccessful) {
                                val user = mAuth.currentUser
                                LogMessage.D(TAG, "user name = $user")
                                startSearchFragment(AuthStatus.AUTH_FACEBOOK)
                            } else {
                                LogMessage.D(TAG, "Log in with facebook account failed")
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


        mBinding.fbLogoutBtn.setOnClickListener {
            // To logout Facebook programmatically
            LoginManager.getInstance().logOut()

            firebaseSingOut()
        }
    }

    private fun sendFbInfoRequest(token: AccessToken) {
        val graphRequest = GraphRequest.newMeRequest(
            token
        ) { jObj, response ->
            try {
                if (response.connection.responseCode == 200) {
                    val id = jObj.getLong("id")
                    val name = jObj.getString("name")
                    val email = jObj.getString("email")
                    Log.d(TAG, "Facebook id:$id")
                    Log.d(TAG, "Facebook name:$name")
                    Log.d(TAG, "Facebook email:$email")
                    // 此時如果登入成功，就可以順便取得用戶大頭照
                    val profile = Profile.getCurrentProfile()
                    // 設定大頭照大小
                    val userPhoto = profile.getProfilePictureUri(300, 300)
//                            Glide.with(this@FacebookActivity)
//                                .load(userPhoto.toString())
//                                .crossFade()
//                                .into(mImgPhoto)

//                    val fbProfile = Profile.getCurrentProfile()
//                    if (fbProfile != null) {
//                        // 取得用戶大頭照
//                        val userPhoto = fbProfile.getProfilePictureUri(300, 300)
//                        val id = fbProfile.id
//                        val name = fbProfile.name
//                        LogMessage.D(TAG, "Facebook userPhoto: $userPhoto")
//                        LogMessage.D(TAG, "Facebook id: $id")
//                        LogMessage.D(TAG, "Facebook name: $name")
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        // https://developers.facebook.com/docs/android/graph?locale=zh_TW
        // 如果要取得email，需透過添加參數的方式來獲取(如下)
        // 不添加只能取得id & name
        val parameters = Bundle()
        parameters.putString("fields", "id,name,email")
        graphRequest.parameters = parameters
        graphRequest.executeAsync()
    }

    private fun initAnonymousAuth() {
        mBinding.anonymousLoginBtn.setOnClickListener {
            mAuth.signInAnonymously().addOnCompleteListener { authResult ->
                if (authResult.isSuccessful) {
                    LogMessage.D(TAG, "匿名登入成功 uid:" + mAuth.currentUser?.uid)
                    startSearchFragment(AuthStatus.AUTH_ANONYMOUS)
                } else {
                    Toast.makeText(context, "匿名登入失敗", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initSetting() {
        checkUserHadLogin()
    }

    private fun checkUserHadLogin() {
        // If google api client is connected, user had logged in with google account before
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            LogMessage.D(TAG, "Has already auth with \"Google\" account")
            LogMessage.D(TAG, "account.idToken: ${account.idToken}")
            LogMessage.D(TAG, "account.displayName: ${account.displayName}")
            LogMessage.D(TAG, "account.email: ${account.email}")
            LogMessage.D(TAG, "account.photoUrl: ${account.photoUrl}")

            startSearchFragment(AuthStatus.AUTH_GOOGLE)
            return
        }

        // If user had logged in facebook, token would not be null
        val token = AccessToken.getCurrentAccessToken()
        if (token != null) {
            LogMessage.D(TAG, "Has already auth with \"Facebook\" account")
            sendFbInfoRequest(token)

            startSearchFragment(AuthStatus.AUTH_FACEBOOK)
            return
        }

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            LogMessage.D(TAG, "Has already auth with \"Anonymous\" account")

            startSearchFragment(AuthStatus.AUTH_ANONYMOUS)
        }
    }

    private fun startSearchFragment(authStatus: Int) {
        LogMessage.D(TAG, "startSearchFragment(), autoStatus: $authStatus")

        mPreferences.authStatus = authStatus

        mBinding.loadText.visibility = View.VISIBLE
        mBinding.googleLoginButton.isEnabled = false
        mBinding.fbLoginButton.isEnabled = false
        mBinding.anonymousLoginBtn.isEnabled = false
        SimpleDelayTask.after(Constants.LOAD_DATA_TIME) {
            Navigation.findNavController(mBinding.root).navigate(R.id.action_mainActivityFragment_to_unboxParityActivity)
        }
    }
}

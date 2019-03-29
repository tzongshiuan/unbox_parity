package com.hsuanparty.unbox_parity.view.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.facebook.*
import com.facebook.login.LoginResult
import com.hsuanparty.unbox_parity.databinding.FragmentMainBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.utils.LogMessage
import javax.inject.Inject
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.hsuanparty.unbox_parity.model.AuthStatus
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import java.io.IOException
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.facebook.FacebookSdk.getApplicationContext
import android.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import io.reactivex.internal.util.HalfSerializer.onComplete


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

    private lateinit var mGoogleApiClient: GoogleApiClient

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

//        mDbManager.setSingleValueEvent()
        mDbManager.setChildEvent()
//        mDbManager.add()
        mDbManager.clearAll()
    }

    private fun initGoogleAuth() {
        mGoogleApiClient = GoogleApiClient.Builder(getApplicationContext())
            .enableAutoManage(activity!!) {
                Toast.makeText(context, "Google", Toast.LENGTH_LONG).show()
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
            .build()

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
                            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
                        }else {
                            Toast.makeText(context, "SingIn name:"+account.displayName, Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, "匿名登入成功 uid:\n" + mAuth.currentUser?.uid, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "匿名登入失敗", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initSetting() {
        // If google api client is connected, user had logged in with google account before
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            LogMessage.D(TAG, "Has already auth with \"Google\" account")
            mPreferences.authStatus = AuthStatus.AUTH_GOOGLE

            LogMessage.D(TAG, "account.idToken: ${account.idToken}")
            LogMessage.D(TAG, "account.displayName: ${account.displayName}")
            LogMessage.D(TAG, "account.email: ${account.email}")
            LogMessage.D(TAG, "account.photoUrl: ${account.photoUrl}")
            return
        }

        // If user had logged in facebook, token would not be null
        val token = AccessToken.getCurrentAccessToken()
        if (token != null) {
            LogMessage.D(TAG, "Has already auth with \"Facebook\" account")
            sendFbInfoRequest(token)
            mPreferences.authStatus = AuthStatus.AUTH_FACEBOOK
            return
        }

        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            LogMessage.D(TAG, "Has already auth with \"Anonymous\" account")
            mPreferences.authStatus = AuthStatus.AUTH_ANONYMOUS
        }
    }
}

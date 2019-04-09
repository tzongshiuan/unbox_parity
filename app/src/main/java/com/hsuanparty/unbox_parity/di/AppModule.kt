package com.hsuanparty.unbox_parity.di

import android.app.Application
import android.content.Context
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.google.firebase.auth.FirebaseAuth
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.model.FirebaseDbManager
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.PreferencesHelper
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Singleton


/**
 * Author: Tsung Hsuan, Lai
 * Created on: 2019/3/27
 */
@Module(includes = [ViewModelModule::class])
class AppModule(private val application: Application) {

//    @Provides
//    @Singleton
//    fun provideApiService(preferencesHelper : PreferencesHelper): ApiService {
//        val ipSelectInterceptor = object : HostSelectionInterceptor(preferencesHelper.ip){
//            override var host:String = preferencesHelper.ip
//                get() = preferencesHelper.ip
//        }
//
//        val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
//            this.level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val client : OkHttpClient = OkHttpClient.Builder().apply {
//            this.addInterceptor(ipSelectInterceptor)
//            this.addInterceptor(interceptor)
//        }.readTimeout(5, TimeUnit.SECONDS)
//         .connectTimeout(5, TimeUnit.SECONDS)
//         .build()
//
//        try {
//            return Retrofit.Builder()
//                    .baseUrl(preferencesHelper.ip)
////                    .baseUrl("http://192.168.10.224:13667/")
////                .baseUrl("http://192.168.10.204:8081/")
////                .baseUrl("http://192.168.10.228:8050/")
//                    .addConverterFactory(GsonConverterFactory.create())
////                .addCallAdapterFactory(LiveDataCallAdapterFactory())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .client(client)   //show log
//                    .build()
//                    .create(ApiService::class.java)
//        }catch (e: Exception){
//            return Retrofit.Builder()
//                    .baseUrl("http://192.168.10.29")// and error server ip
////                    .baseUrl(preferencesHelper.getServerIp())
////                .baseUrl("http://192.168.10.204:8081/")
////                .baseUrl("http://192.168.10.228:8050/")
//                    .addConverterFactory(GsonConverterFactory.create())
////                .addCallAdapterFactory(LiveDataCallAdapterFactory())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .client(client)   //show log
//                    .build()
//                    .create(ApiService::class.java)
//        }
//    }

    @Provides
    @Singleton
    fun provideApplication(): Application {
        return application
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(myPreferences: MyPreferences): PreferencesHelper {
        return myPreferences
    }

    @Provides
    @Singleton
    fun provideCompositeDisposable(): CompositeDisposable {
        return CompositeDisposable()
    }

    @Provides
    @Singleton
    fun provideFirebaseDbManager(mAuth: FirebaseAuth): FirebaseDbManager {
        return FirebaseDbManager(mAuth)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideCallbackManager(): CallbackManager {
        return CallbackManager.Factory.create()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInOptions(application: Application): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("313077683472-kh78ota6sndb9ma926jbn1un3s35ju42.apps.googleusercontent.com")
                .requestEmail()
                .build()
    }

    @Provides
    @Singleton
    fun provideGoogleAccountCredential(application: Application): GoogleAccountCredential {
        val credential = GoogleAccountCredential.usingOAuth2(application, Arrays.asList(YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_READONLY))
        credential.backOff = ExponentialBackOff()
        return credential
    }
}
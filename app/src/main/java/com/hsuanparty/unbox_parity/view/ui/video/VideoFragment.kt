package com.hsuanparty.unbox_parity.view.ui.video

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.hsuanparty.unbox_parity.R

import com.hsuanparty.unbox_parity.databinding.SearchFragmentBinding
import com.hsuanparty.unbox_parity.databinding.VideoFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import javax.inject.Inject

class VideoFragment : Fragment(), Injectable{

    companion object {
        private val TAG = VideoFragment::class.java.simpleName
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    private lateinit var viewModel: VideoViewModel

    private lateinit var mBinding: VideoFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = VideoFragmentBinding.inflate(inflater, container, false)
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)

        viewModel.isPerformExitFullScreen.observe(this, Observer { isExit ->
            if (isExit) {
                mBinding.youtubeView.exitFullScreen()
            }
        })
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
    }

    private fun initUI() {
        lifecycle.addObserver(mBinding.youtubeView)
        mBinding.youtubeView.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                LogMessage.D(TAG, "onReady()")

                val videoId = "iit92tkX5wI"
                youTubePlayer.cueVideo(videoId, 0f)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                LogMessage.D(TAG, "onStateChange(), state: $state")

                when (state) {
                    PlayerConstants.PlayerState.ENDED -> {
                        val videoId = "iit92tkX5wI"
                        youTubePlayer.cueVideo(videoId, 0f)
                    }

                    else -> {}
                }
            }
        })

        mBinding.youtubeView.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                LogMessage.D(TAG, "Player Enter FullScreen")

                viewModel.enterFullScreen()

//                val controller = mBinding.youtubeView.getPlayerUiController()
//                val exitBtn = Button(context)
//                exitBtn.text = "Exit"
//                exitBtn.setOnClickListener {
//                    LogMessage.D(TAG, "Click exit fullscreen button")
//                    mBinding.youtubeView.exitFullScreen()
//                }
//                controller.addView(exitBtn)
            }

            override fun onYouTubePlayerExitFullScreen() {
                LogMessage.D(TAG, "Player Exit FullScreen")

                viewModel.exitFullScreen()
            }
        })
    }
}

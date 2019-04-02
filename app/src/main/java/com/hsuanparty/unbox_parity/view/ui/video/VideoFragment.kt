package com.hsuanparty.unbox_parity.view.ui.video

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hsuanparty.unbox_parity.databinding.VideoFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.youtube.YoutubeAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import javax.inject.Inject

class VideoFragment : Fragment(), Injectable{

    companion object {
        private val TAG = VideoFragment::class.java.simpleName
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var mPreferences: MyPreferences

    private lateinit var viewModel: VideoViewModel

    private lateinit var mBinding: VideoFragmentBinding

    private var player: YouTubePlayer? = null

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
        LogMessage.D(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, factory).get(VideoViewModel::class.java)
        (mBinding.recyclerView.adapter as YoutubeAdapter).videoViewModel = viewModel

        viewModel.isPerformExitFullScreen.observe(this, Observer { isExit ->
            if (isExit) {
//                mBinding.youtubeView.exitFullScreen()
            }
        })

        viewModel.videoSearchResult.observe(this, Observer { result ->
            (mBinding.recyclerView.adapter as YoutubeAdapter).mVideoList = result
            mBinding.recyclerView.adapter?.notifyDataSetChanged()

            viewModel.searchVideoFinished.postValue(true)
        })

        viewModel.curVideoItem.observe(this, Observer { videoItem ->
            mBinding.recyclerView.adapter?.notifyDataSetChanged()
            player?.loadVideo(videoItem.id!!, 0f)
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

        mPreferences.curVideoItem = null
        player = null
        ((mBinding.recyclerView.adapter as YoutubeAdapter).mVideoList as ArrayList).clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = RecyclerView.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = YoutubeAdapter()

        lifecycle.addObserver(mBinding.youtubeView)
        mBinding.youtubeView.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                LogMessage.D(TAG, "onReady()")

                player = youTubePlayer
                val videoId = mPreferences.curVideoItem?.id
                if (!videoId.isNullOrEmpty()) {
                    youTubePlayer.loadVideo(videoId, 0f)
                } else {
                    youTubePlayer.cueVideo("", 0f)
                }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                LogMessage.D(TAG, "onStateChange(), state: $state")

                when (state) {
                    PlayerConstants.PlayerState.ENDED -> {
                        youTubePlayer.cueVideo(mPreferences.curVideoItem?.id!!, 0f)
                    }

                    else -> {}
                }
            }
        })

        mBinding.youtubeView.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                LogMessage.D(TAG, "Player Enter FullScreen")

                viewModel.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                LogMessage.D(TAG, "Player Exit FullScreen")

                viewModel.exitFullScreen()
            }
        })
    }
}

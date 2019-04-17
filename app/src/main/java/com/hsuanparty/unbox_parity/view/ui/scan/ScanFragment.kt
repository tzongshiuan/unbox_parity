package com.hsuanparty.unbox_parity.view.ui.scan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.googlecode.tesseract.android.TessBaseAPI
import com.hsuanparty.unbox_parity.R
import com.hsuanparty.unbox_parity.databinding.ScanFragmentBinding
import com.hsuanparty.unbox_parity.di.Injectable
import com.hsuanparty.unbox_parity.model.MyPreferences
import com.hsuanparty.unbox_parity.model.OcrResult
import com.hsuanparty.unbox_parity.model.OcrResultFailure
import com.hsuanparty.unbox_parity.model.OcrResultText
import com.hsuanparty.unbox_parity.utils.Constants
import com.hsuanparty.unbox_parity.utils.LogMessage
import com.hsuanparty.unbox_parity.utils.MyViewModelFactory
import com.hsuanparty.unbox_parity.utils.text_ocr.*
import com.hsuanparty.unbox_parity.view.ui.UnboxParityActivity
import kotlinx.android.synthetic.main.scan_fragment.*
import java.io.File
import java.io.IOException
import javax.inject.Inject


class ScanFragment : Fragment(), Injectable, SurfaceHolder.Callback, ShutterButton.OnShutterButtonListener {

    companion object {
        private val TAG = ScanFragment::class.java.simpleName

        /** ISO 639-3 language code indicating the default recognition language.  */
        const val DEFAULT_SOURCE_LANGUAGE_CODE = "eng+chi_tra"

        /** ISO 639-1 language code indicating the default target language for translation.  */
        const val DEFAULT_TARGET_LANGUAGE_CODE = "es"

        /** The default OCR engine to use.  */
        const val DEFAULT_OCR_ENGINE_MODE = "Tesseract"

        /** The default page segmentation mode to use.  */
        const val DEFAULT_PAGE_SEGMENTATION_MODE = "Auto"

        /** Whether to use autofocus by default.  */
        const val DEFAULT_TOGGLE_AUTO_FOCUS = true

        /** Whether to initially disable continuous-picture and continuous-video focus modes.  */
        const val DEFAULT_DISABLE_CONTINUOUS_FOCUS = true

        /** Whether to beep by default when the shutter button is pressed.  */
        const val DEFAULT_TOGGLE_BEEP = false

        /** Whether to initially show a looping, real-time OCR display.  */
        const val DEFAULT_TOGGLE_CONTINUOUS = false

        /** Whether to initially reverse the image returned by the camera.  */
        const val DEFAULT_TOGGLE_REVERSED_IMAGE = false

        /** Whether to enable the use of online translation services be default.  */
        const val DEFAULT_TOGGLE_TRANSLATION = true

        /** Whether the light should be initially activated by default.  */
        const val DEFAULT_TOGGLE_LIGHT = false


        /** Flag to display the real-time recognition results at the top of the scanning screen.  */
        private const val CONTINUOUS_DISPLAY_RECOGNIZED_TEXT = true

        /** Flag to display recognition-related statistics on the scanning screen.  */
        private const val CONTINUOUS_DISPLAY_METADATA = true

        /** Flag to enable display of the on-screen shutter button.  */
        private const val DISPLAY_SHUTTER_BUTTON = true

        /** Resource to use for data file downloads.  */
        const val DOWNLOAD_BASE = "http://tesseract-ocr.googlecode.com/files/"

        /** Download filename for orientation and script detection (OSD) data.  */
        const val OSD_FILENAME = "tesseract-ocr-3.01.osd.tar"

        /** Destination filename for orientation and script detection (OSD) data.  */
        const val OSD_FILENAME_BASE = "osd.traineddata"

        /** Languages for which Cube data is available.  */
        val CUBE_SUPPORTED_LANGUAGES = arrayOf(
            "ara", // Arabic
            "eng", // English
            "hin" // Hindi
        )
    }

    @Inject
    lateinit var factory: MyViewModelFactory

    @Inject
    lateinit var mPreferences: MyPreferences

    private lateinit var viewModel: ScanViewModel

    private lateinit var mBinding: ScanFragmentBinding

    private lateinit var cameraManager: CameraManager

    private lateinit var baseApi: TessBaseAPI  // Java interface for the Tesseract OCR engine

    private lateinit var surfaceHolder: SurfaceHolder

    private var handler: ScanHandler? = null

    private var lastResult: OcrResult? = null

    private var isEngineReady = false
    private var isPaused = false
    private var hasSurface = false

    private var isEnableScan = false

    private var ocrEngineMode = TessBaseAPI.OEM_TESSERACT_ONLY

    private var sourceLanguageCodeOcr: String? = null // ISO 639-3 language code
    private var sourceLanguageReadable: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LogMessage.D(TAG, "onCreateView()")
        mBinding = ScanFragmentBinding.inflate(inflater, container, false)
        initSetting()
        initUI()

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        LogMessage.D(TAG, "onActivityCreated()")
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, factory).get(ScanViewModel::class.java)
        viewModel.isEnableOCR.observe(this, Observer { isEnable ->
            isEnableScan = isEnable

            if (isEnable) {
                resumeOCR()
            } else {
                LogMessage.D(TAG, "Leave scan mode")
                handler?.quitSynchronously()

                // Stop using the camera, to avoid conflicting with other camera-based apps
                cameraManager.closeDriver()
            }
        })
    }

    override fun onResume() {
        LogMessage.D(TAG, "onResume()")
        super.onResume()

        if (isEnableScan) {
            resumeOCR()
        }
    }

    override fun onPause() {
        LogMessage.D(TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        LogMessage.D(TAG, "onStop()")
        super.onStop()

        if (isEnableScan) {
            handler?.quitSynchronously()
            cameraManager.closeDriver()
        }
    }

    override fun onDestroy() {
        LogMessage.D(TAG, "onDestroy()")
        super.onDestroy()

        if (!hasSurface) {
            val surfaceHolder = mBinding.previewView.holder
            surfaceHolder.removeCallback(this)
        }

        baseApi.end()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LogMessage.D(TAG, "onActivityResult()")
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initUI() {
        mBinding.retryBtn.setOnClickListener {
            mBinding.ocrResultTextView.visibility = View.GONE
            mBinding.imageView.visibility = View.GONE

            mBinding.shutterBtn.visibility = View.VISIBLE
            mBinding.viewFinder.visibility = View.VISIBLE

            mBinding.retryBtn.visibility = View.GONE
            mBinding.confirmBtn.visibility = View.GONE
        }

        mBinding.confirmBtn.setOnClickListener {
            mBinding.retryBtn.visibility = View.GONE
            mBinding.confirmBtn.visibility = View.GONE

            (activity as UnboxParityActivity).closeScanPage(mBinding.ocrResultTextView.text.toString())
        }

        mBinding.viewFinder.setOnTouchListener(object : View.OnTouchListener {
            var lastX = -1
            var lastY = -1

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = -1
                        lastY = -1
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val currentX = event.x.toInt()
                        val currentY = event.y.toInt()

                        try {
                            val rect = cameraManager.framingRect

                            val buffer = 50
                            val bigBuffer = 60
                            if (lastX >= 0) {
                                // Adjust the size of the viewfinder rectangle. Check if the touch event occurs in the corner areas first, because the regions overlap.
                                if ((currentX >= rect.left - bigBuffer && currentX <= rect.left + bigBuffer || lastX >= rect.left - bigBuffer && lastX <= rect.left + bigBuffer) && (currentY <= rect.top + bigBuffer && currentY >= rect.top - bigBuffer || lastY <= rect.top + bigBuffer && lastY >= rect.top - bigBuffer)) {
                                    // Top left corner: adjust both top and left sides
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (lastY - currentY))
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentX >= rect.right - bigBuffer && currentX <= rect.right + bigBuffer || lastX >= rect.right - bigBuffer && lastX <= rect.right + bigBuffer) && (currentY <= rect.top + bigBuffer && currentY >= rect.top - bigBuffer || lastY <= rect.top + bigBuffer && lastY >= rect.top - bigBuffer)) {
                                    // Top right corner: adjust both top and right sides
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (lastY - currentY))
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentX >= rect.left - bigBuffer && currentX <= rect.left + bigBuffer || lastX >= rect.left - bigBuffer && lastX <= rect.left + bigBuffer) && (currentY <= rect.bottom + bigBuffer && currentY >= rect.bottom - bigBuffer || lastY <= rect.bottom + bigBuffer && lastY >= rect.bottom - bigBuffer)) {
                                    // Bottom left corner: adjust both bottom and left sides
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 2 * (currentY - lastY))
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentX >= rect.right - bigBuffer && currentX <= rect.right + bigBuffer || lastX >= rect.right - bigBuffer && lastX <= rect.right + bigBuffer) && (currentY <= rect.bottom + bigBuffer && currentY >= rect.bottom - bigBuffer || lastY <= rect.bottom + bigBuffer && lastY >= rect.bottom - bigBuffer)) {
                                    // Bottom right corner: adjust both bottom and right sides
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 2 * (currentY - lastY))
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentX >= rect.left - buffer && currentX <= rect.left + buffer || lastX >= rect.left - buffer && lastX <= rect.left + buffer) && (currentY <= rect.bottom && currentY >= rect.top || lastY <= rect.bottom && lastY >= rect.top)) {
                                    // Adjusting left side: event falls within BUFFER pixels of left side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (lastX - currentX), 0)
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentX >= rect.right - buffer && currentX <= rect.right + buffer || lastX >= rect.right - buffer && lastX <= rect.right + buffer) && (currentY <= rect.bottom && currentY >= rect.top || lastY <= rect.bottom && lastY >= rect.top)) {
                                    // Adjusting right side: event falls within BUFFER pixels of right side, and between top and bottom side limits
                                    cameraManager.adjustFramingRect(2 * (currentX - lastX), 0)
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentY <= rect.top + buffer && currentY >= rect.top - buffer || lastY <= rect.top + buffer && lastY >= rect.top - buffer) && (currentX <= rect.right && currentX >= rect.left || lastX <= rect.right && lastX >= rect.left)) {
                                    // Adjusting top side: event falls within BUFFER pixels of top side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (lastY - currentY))
                                    //mBinding.viewFinder.removeResultText()
                                } else if ((currentY <= rect.bottom + buffer && currentY >= rect.bottom - buffer || lastY <= rect.bottom + buffer && lastY >= rect.bottom - buffer) && (currentX <= rect.right && currentX >= rect.left || lastX <= rect.right && lastX >= rect.left)) {
                                    // Adjusting bottom side: event falls within BUFFER pixels of bottom side, and between left and right side limits
                                    cameraManager.adjustFramingRect(0, 2 * (currentY - lastY))
                                    //mBinding.viewFinder.removeResultText()
                                }
                            }
                        } catch (e: NullPointerException) {
                            Log.e(TAG, "Framing rect not available", e)
                        }

                        v?.invalidate()
                        lastX = currentX
                        lastY = currentY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        lastX = -1
                        lastY = -1
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun initSetting() {
        // status bar height
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        cameraManager = CameraManager(context, statusBarHeight)
        mBinding.viewFinder.setCameraManager(cameraManager)

        mBinding.shutterBtn.setOnShutterButtonListener(this)

        sourceLanguageCodeOcr = DEFAULT_SOURCE_LANGUAGE_CODE
        sourceLanguageReadable = LanguageCodeHelper.getOcrLanguageName(context, DEFAULT_SOURCE_LANGUAGE_CODE)

        surfaceHolder = mBinding.previewView.holder
        if (!hasSurface) {
            surfaceHolder.addCallback(this)
        }

        // Initialize the OCR engine
        val storageDirectory = getStorageDirectory()
        if (storageDirectory != null) {
            initOcrEngine(storageDirectory, sourceLanguageCodeOcr!!, sourceLanguageReadable!!)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "surfaceCreated()")

        if (holder == null) {
            Log.e(TAG, "surfaceCreated gave us a null surface")
        }

        // Only initialize the camera if the OCR engine is ready to go.
        if (!hasSurface && isEngineReady) {
            Log.d(TAG, "surfaceCreated(): calling initCamera()...")
//            initCamera(holder)
        }
        hasSurface = true

        if (isEnableScan) {
            resumeOCR()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed()")
        hasSurface = false
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged()")
    }

    override fun onShutterButtonFocus(b: ShutterButton?, pressed: Boolean) {
        requestDelayedAutoFocus()
    }

    private fun requestDelayedAutoFocus() {
        // Wait 350 ms before focusing to avoid interfering with quick button presses when
        // the user just wants to take a picture without focusing.
        cameraManager.requestAutoFocus(350L)
    }

    override fun onShutterButtonClick(b: ShutterButton?) {
//        if (isContinuousModeActive) {
//            onShutterButtonPressContinuous()
//        } else {
            handler?.shutterButtonClick()
//        }
    }

    /** Finds the proper location on the SD card where we can save files.  */
    private fun getStorageDirectory(): File? {
        //LogMessage.D(TAG, "getStorageDirectory(): API level is " + Integer.valueOf(android.os.Build.VERSION.SDK_INT));

        var state: String? = null
        try {
            state = Environment.getExternalStorageState()
        } catch (e: RuntimeException) {
            LogMessage.E(TAG, "Is the SD card visible?", e)
            LogMessage.E(TAG, "Required external storage (such as an SD card) is unavailable.")
        }

        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            // We can read and write the media
            //    	if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
            // For Android 2.2 and above
            try {
                return activity?.getExternalFilesDir(Environment.MEDIA_MOUNTED)
            } catch (e: NullPointerException) {
                // We get an error here if the SD card is visible, but full
                LogMessage.E(TAG, "External storage is unavailable")
                LogMessage.E(TAG, "Required external storage (such as an SD card) is full or unavailable.")
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY == state) {
            // We can only read the media
            LogMessage.E(TAG, "External storage is read-only")
            LogMessage.E(TAG, "Required external storage (such as an SD card) is unavailable for data storage.")
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // to know is we can neither read nor write
            LogMessage.E(TAG, "External storage is unavailable")
            LogMessage.E(TAG, "Required external storage (such as an SD card) is unavailable or corrupted.")
        }
        return null
    }

    private var dialog: ProgressDialog? = null // for initOcr - language download & unzip
    private var indeterminateDialog: ProgressDialog? = null
    //private val indeterminateDialog: ProgressDialog? = null // also for initOcr - init OCR engine
    private fun initOcrEngine(storageRoot: File, languageCode: String, languageName: String) {
        isEngineReady = false

        // Set up the dialog box for the thermometer-style download progress indicator
        if (dialog != null) {
            dialog?.dismiss()
        }
        dialog = ProgressDialog(context)

        // Display the name of the OCR engine we're initializing in the indeterminate progress dialog box
//        indeterminateDialog = ProgressDialog(context)
//        indeterminateDialog!!.setTitle("Please wait")
//        val ocrEngineModeName = getOcrEngineModeName()
//        if (ocrEngineModeName == "Both") {
//            indeterminateDialog!!.setMessage("Initializing Cube and Tesseract OCR engines for $languageName...")
//        } else {
//            indeterminateDialog!!.setMessage("Initializing $ocrEngineModeName OCR engine for $languageName...")
//        }
//        indeterminateDialog!!.setCancelable(false)
//        indeterminateDialog!!.show()

        handler?.quitSynchronously()

        // Disable continuous mode if we're using Cube. This will prevent bad states for devices
        // with low memory that crash when running OCR with Cube, and prevent unwanted delays.
//        if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY || ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
//            LogMessage.D(TAG, "Disabling continuous preview")
//            isContinuousModeActive = false
//            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
//            prefs.edit().putBoolean(PreferencesActivity.KEY_CONTINUOUS_PREVIEW, false)
//        }

        // Start AsyncTask to install language data and init OCR
        baseApi = TessBaseAPI()
        OcrInitAsyncTask(this, baseApi, dialog, indeterminateDialog, languageCode, languageName, ocrEngineMode)
            .execute(storageRoot.toString())
    }

    private fun resumeOCR() {
        LogMessage.D(TAG, "resumeOCR()")

        // This method is called when Tesseract has already been successfully initialized, so set 
        // isEngineReady = true here.
        isEngineReady = true

        isPaused = false

        if (handler != null) {
            handler?.resetState()
        }

        mBinding.ocrResultTextView.visibility = View.GONE
        mBinding.imageView.visibility = View.GONE

        mBinding.shutterBtn.visibility = View.VISIBLE
        mBinding.viewFinder.visibility = View.VISIBLE

        mBinding.retryBtn.visibility = View.GONE
        mBinding.confirmBtn.visibility = View.GONE

        if (baseApi != null) {
            baseApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
//            baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, characterBlacklist)
//            baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, characterWhitelist)
        }

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        }
    }

    /** Initializes the camera and starts the handler to begin previewing.  */
    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        Log.d(TAG, "initCamera()")
        if (surfaceHolder == null) {
            throw IllegalStateException("No SurfaceHolder provided")
        }
        try {

            // Open and initialize the camera
            cameraManager.openDriver(surfaceHolder)

            // Creating the handler starts the preview, which can also throw a RuntimeException.
            handler = ScanHandler(this, cameraManager, false)

        } catch (ioe: IOException) {
            showErrorMessage("Error", "Could not initialize camera. Please try restarting device.")
        } catch (e: RuntimeException) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            showErrorMessage("Error", "Could not initialize camera. Please try restarting device.")
        }
    }

    /** Displays a pop-up message showing the name of the current OCR source language.  */
    fun showLanguageName() {
        if (Constants.IS_DEBUG_MODE) {
            val toast = Toast.makeText(context, "OCR: $sourceLanguageReadable", Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
    }

    fun showErrorMessage(title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setOnCancelListener(FinishListener(activity))
            .setPositiveButton("Done", FinishListener(activity))
            .show()
    }

    fun getBaseApi(): TessBaseAPI {
        return baseApi
    }

    fun getCameraManager(): CameraManager {
        return cameraManager
    }

    fun getHandler(): Handler {
        return handler!!
    }

    fun displayProgressDialog() {
        // Set up the indeterminate progress dialog box
        indeterminateDialog = ProgressDialog(context)
        indeterminateDialog?.setTitle(getString(R.string.txt_waiting))
        indeterminateDialog?.setMessage(getString(R.string.msg_perform_text_recognition))
        indeterminateDialog?.setCancelable(false)
        indeterminateDialog?.show()
    }

    fun getProgressDialog(): ProgressDialog? {
        return indeterminateDialog
    }

    /**
     * Returns a string that represents which OCR engine(s) are currently set to be run.
     *
     * @return OCR engine mode
     */
    internal fun getOcrEngineModeName(): String {
        var ocrEngineModeName = ""
        val ocrEngineModes = resources.getStringArray(R.array.ocrenginemodes)
        if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_ONLY) {
            ocrEngineModeName = ocrEngineModes[0]
        } else if (ocrEngineMode == TessBaseAPI.OEM_CUBE_ONLY) {
            ocrEngineModeName = ocrEngineModes[1]
        } else if (ocrEngineMode == TessBaseAPI.OEM_TESSERACT_CUBE_COMBINED) {
            ocrEngineModeName = ocrEngineModes[2]
        }
        return ocrEngineModeName
    }

    fun stopHandler() {
        if (handler != null) {
            handler?.stop()
        }
    }

    fun setButtonVisibility(visible: Boolean) {
        if (visible && DISPLAY_SHUTTER_BUTTON) {
            mBinding.shutterBtn.visibility = View.VISIBLE
        } else {
            mBinding.shutterBtn.visibility = View.GONE
        }
    }

    fun setStatusViewForContinuous() {
        mBinding.viewFinder.removeResultText()
//        if (CONTINUOUS_DISPLAY_METADATA) {
            //statusViewBottom.setText("OCR: $sourceLanguageReadable - waiting for OCR...")
//        }
    }

    private fun setSpanBetweenTokens(
        text: CharSequence, token: String,
        vararg cs: CharacterStyle
    ): CharSequence {
        var text = text
        // Start and end refer to the points where the span will apply
        val tokenLen = token.length
        val start = text.toString().indexOf(token) + tokenLen
        val end = text.toString().indexOf(token, start)

        if (start > -1 && end > -1) {
            // Copy the spannable string to a mutable spannable string
            val ssb = SpannableStringBuilder(text)
            for (c in cs)
                ssb.setSpan(c, start, end, 0)
            text = ssb
        }
        return text
    }

    /**
     * Version of handleOcrContinuousDecode for failed OCR requests. Displays a failure message.
     *
     * @param obj Metadata for the failed OCR request.
     */
    fun handleOcrContinuousDecode(obj: OcrResultFailure) {
        lastResult = null
        mBinding.viewFinder.removeResultText()

        // Reset the text in the recognized text box.
//        statusViewTop.text = ""
//
//        if (CONTINUOUS_DISPLAY_METADATA) {
//            // Color text delimited by '-' as red.
//            statusViewBottom.textSize = 14f
//            val cs = setSpanBetweenTokens(
//                "OCR: " + sourceLanguageReadable + " - OCR failed - Time required: "
//                        + obj.timeRequired + " ms", "-", ForegroundColorSpan(-0x10000)
//            )
//            statusViewBottom.text = cs
//        }
    }

    /**
     * Displays information relating to the results of a successful real-time OCR request.
     *
     * @param ocrResult Object representing successful OCR results
     */
    fun handleOcrContinuousDecode(ocrResult: OcrResult) {
        lastResult = ocrResult

        // Send an OcrResultText object to the ViewfinderView for text rendering
        mBinding.viewFinder.addResultText(
            OcrResultText(
                ocrResult.text,
                ocrResult.wordConfidences,
                ocrResult.meanConfidence,
                ocrResult.bitmapDimensions,
                ocrResult.regionBoundingBoxes,
                ocrResult.textlineBoundingBoxes,
                ocrResult.stripBoundingBoxes,
                ocrResult.wordBoundingBoxes,
                ocrResult.characterBoundingBoxes
            )
        )

        val meanConfidence = ocrResult.meanConfidence

//        if (CONTINUOUS_DISPLAY_RECOGNIZED_TEXT) {
//            // Display the recognized text on the screen
//            mBinding.statusViewTop.text = ocrResult.text
//            val scaledSize = Math.max(22, 32 - ocrResult.text.length / 4)
//            mBinding.statusViewTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())
//            mBinding.statusViewTop.setTextColor(Color.BLACK)
//            mBinding.statusViewTop.setBackgroundResource(R.color.white)
//
//            mBinding.statusViewTop.background.alpha = meanConfidence * (255 / 100)
//        }
//
//        if (CONTINUOUS_DISPLAY_METADATA) {
//            // Display recognition-related metadata at the bottom of the screen
//            val recognitionTimeRequired = ocrResult.recognitionTimeRequired
//            mBinding.statusViewBottom.textSize = 14f
//            mBinding.statusViewBottom.text = "OCR: " + sourceLanguageReadable + " - Mean confidence: " +
//                    meanConfidence!!.toString() + " - Time required: " + recognitionTimeRequired + " ms"
//        }
    }

    fun setShutterButtonClickable(clickable: Boolean) {
        mBinding.shutterBtn.isClickable = clickable
    }

    fun drawViewfinder() {
        mBinding.viewFinder.drawViewfinder()
    }

    fun handleOcrDecode(ocrResult: OcrResult): Boolean {
        lastResult = ocrResult

        // Test whether the result is null
        if (ocrResult.text == null || ocrResult.text.equals("")) {
            val toast = Toast.makeText(context, "OCR failed. Please try again.", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
            return false
        }

        mBinding.ocrResultTextView.visibility = View.VISIBLE
        mBinding.imageView.visibility = View.VISIBLE

        // Turn off capture-related UI elements
        mBinding.shutterBtn.visibility = View.GONE
        mBinding.viewFinder.visibility = View.GONE
        mBinding.retryBtn.visibility = View.VISIBLE
        mBinding.confirmBtn.visibility = View.VISIBLE

//        statusViewBottom.visibility = View.GONE
//        statusViewTop.visibility = View.GONE
//        cameraButtonView.setVisibility(View.GONE)
//        resultView.setVisibility(View.VISIBLE)

        val lastBitmap = ocrResult.bitmap
        if (lastBitmap != null && Constants.IS_DEBUG_MODE) {
            mBinding.imageView.setImageBitmap(lastBitmap)
        }

        // Display the recognized text
//        val sourceLanguageTextView = findViewById(R.id.source_language_text_view) as TextView
//        mBinding.sourceLanguageTextView.text = sourceLanguageReadable

        LogMessage.D(TAG, "result: ${ocrResult.text}")
        mBinding.ocrResultTextView.text = ocrResult.text
        // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
        val scaledSize = Math.max(22, 32 - ocrResult.text.length / 4)
        mBinding.ocrResultTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())

//        val translationLanguageLabelTextView = findViewById(R.id.translation_language_label_text_view) as TextView
//        val translationLanguageTextView = findViewById(R.id.translation_language_text_view) as TextView
//        val translationTextView = findViewById(R.id.translation_text_view) as TextView
//        if (isTranslationActive) {
//            // Handle translation text fields
//            translationLanguageLabelTextView.visibility = View.VISIBLE
//            translationLanguageTextView.setText(targetLanguageReadable)
//            translationLanguageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
//            translationLanguageTextView.visibility = View.VISIBLE
//
//            // Activate/re-activate the indeterminate progress indicator
//            translationTextView.visibility = View.GONE
//            progressView.setVisibility(View.VISIBLE)
//            setProgressBarVisibility(true)
//
//            // Get the translation asynchronously
//            TranslateAsyncTask(
//                this, sourceLanguageCodeTranslation, targetLanguageCodeTranslation,
//                ocrResult.text
//            ).execute()
//        } else {
//            translationLanguageLabelTextView.visibility = View.GONE
//            translationLanguageTextView.visibility = View.GONE
//            translationTextView.visibility = View.GONE
//            progressView.setVisibility(View.GONE)
//            setProgressBarVisibility(false)
//        }
        return true
    }

//    private fun scan() {
//        object : Thread() {
//            override fun run() {
//                val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_test)
//                val ocrApi = TessBaseAPI()
//                ocrApi.init(Constants.TESSBASE_PATH, Constants.DEFAULT_LANGUAGE + "+" + Constants.CHINESE_LANGUAGE)
//                ocrApi.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO
//                ocrApi.setImage(bitmap)
//
//                val result = ocrApi.utF8Text
//                LogMessage.D(TAG, "Text OCR result: $result")
//
//                ocrApi.clear()
//                ocrApi.end()
//            }
//        }.start()
//    }

//    private fun convertGray(bitmap: Bitmap): Bitmap {
//        val colorMatrix = ColorMatrix()
//        colorMatrix.setSaturation(0f)
//        val filter = ColorMatrixColorFilter(colorMatrix)
//
//        val paint = Paint()
//        paint.colorFilter = filter
//        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(result)
//        canvas.drawBitmap(bitmap, 0f, 0f, paint)
//
//        return result
//    }
}

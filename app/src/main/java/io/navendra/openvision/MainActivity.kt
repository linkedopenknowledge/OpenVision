package io.navendra.openvision

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2{

    val TAG = MainActivity::class.java.simpleName
    var  mOpenCvCameraView : CameraBridgeViewBase? = null

    private val mIsColorSelected = false
    private val mRgba: Mat? = null
    private val mBlobColorRgba: Scalar? = null
    private val mBlobColorHsv: Scalar? = null
//    private val mDetector: ColorBlobDetector? = null
    private val mSpectrum: Mat? = null
    private val SPECTRUM_SIZE: Size? = null
    private val CONTOUR_COLOR: Scalar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        mOpenCvCameraView = HelloOpenCvView as CameraBridgeViewBase
        mOpenCvCameraView?.apply {
            visibility = SurfaceView.VISIBLE
            setCvCameraViewListener(this@MainActivity)
        }
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView?.enableView()
//                    mOpenCvCameraView?.setOnTouchListener(this@MainActivity as View.OnTouchListener)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        }else{
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }

    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()

    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        if(inputFrame == null){
            Log.e(TAG, "Input frame is null!!")
        }
        return inputFrame!!.gray()
    }

}

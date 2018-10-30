package io.navendra.openvision

import android.content.Context
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Environment
import android.util.Size
import android.support.v4.app.ActivityCompat
import android.os.HandlerThread
import android.os.Handler
import kotlinx.android.synthetic.main.activity_camera.*
import android.view.Surface
import java.util.*
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import android.graphics.Bitmap




class CameraActivity : AppCompatActivity() {

    private lateinit var cameraManager : CameraManager
    private lateinit var surfaceTextureListener: TextureView.SurfaceTextureListener
    private lateinit var previewSize :Size
    private lateinit var stateCallback : CameraDevice.StateCallback
    private  var backgroundThread : HandlerThread? = null
    private  var backgroundHandler : Handler? = null
    private  var captureRequestBuilder : CaptureRequest.Builder? = null
    private var galleryFolder : File? = null
    private lateinit var captureRequest : CaptureRequest
    private var cameraCaptureSession : CameraCaptureSession? = null


    private var cameraId : String = ""
    private var cameraDevice: CameraDevice? = null
    private var cameraFacing : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        initManager()
        createImageGallery()
        floatingActionButton.setOnClickListener {
            onPhotoClicked()
        }
    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        if (textureView.isAvailable()) {
            setUpCamera()
            openCamera()
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener)
        }
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }

    private fun initManager(){
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                setUpCamera()
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

            }
        }

        stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                this@CameraActivity.cameraDevice = cameraDevice
                createPreviewSession()
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                cameraDevice.close()
                this@CameraActivity.cameraDevice = null
            }

            override fun onError(cameraDevice: CameraDevice, error: Int) {
                cameraDevice.close()
                this@CameraActivity.cameraDevice = null
            }
        }

    }

    private fun setUpCamera() {
        try {
            for (cameraId in cameraManager.getCameraIdList()) {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) === cameraFacing) {
                    val streamConfigurationMap = cameraCharacteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                    )
                    previewSize = streamConfigurationMap!!.getOutputSizes(SurfaceTexture::class.java)[0]
                    this.cameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun openCamera(){
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession!!.close()
            cameraCaptureSession = null
        }

        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread!!.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("camera_background_thread")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.getLooper())
    }

    private fun createPreviewSession() {
        try {
            val surfaceTexture = textureView.surfaceTexture
            surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
            val previewSurface = Surface(surfaceTexture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(previewSurface)

            cameraDevice?.createCaptureSession(
                Collections.singletonList(previewSurface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) {
                            return
                        }

                        try {
                            captureRequest = captureRequestBuilder?.build()!!
                            this@CameraActivity.cameraCaptureSession = cameraCaptureSession
                            this@CameraActivity.cameraCaptureSession?.setRepeatingRequest(
                                captureRequest,
                                null,
                                backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

                    }
                }, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun createImageGallery() {
        val storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        galleryFolder = File(storageDirectory, resources.getString(R.string.app_name))
        if (!galleryFolder!!.exists()) {
            val wasCreated = galleryFolder?.mkdirs()!!
            if (!wasCreated) {
                Log.e("CapturedImages", "Failed to create directory")
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(galleryFolder: File): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "image_" + timeStamp + "_"
        return File.createTempFile(imageFileName, ".jpg", galleryFolder)
    }

    fun onPhotoClicked(){
        var outputPhoto: FileOutputStream? = null
        try {
            outputPhoto = FileOutputStream(createImageFile(galleryFolder!!))
            textureView.bitmap
                .compress(Bitmap.CompressFormat.PNG, 100, outputPhoto)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputPhoto?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

}


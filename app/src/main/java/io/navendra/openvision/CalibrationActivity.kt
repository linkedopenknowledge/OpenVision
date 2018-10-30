package io.navendra.openvision

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File


class CalibrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)
    }

    lateinit var s : Settings
    lateinit var inputFile : File
    val inputFilePath = ""

    lateinit var moshi: Moshi

    fun openFile(){
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        

    }
}
